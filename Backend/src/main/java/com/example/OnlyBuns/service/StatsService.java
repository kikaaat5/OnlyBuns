package com.example.OnlyBuns.service;

import com.example.OnlyBuns.dto.WeeklyStatsDto;
import com.example.OnlyBuns.model.Client;
import com.example.OnlyBuns.repository.CommentRepository;
import com.example.OnlyBuns.repository.FollowRelationRepository;
import com.example.OnlyBuns.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class StatsService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private FollowRelationRepository followRelationRepository;

    @Autowired
    private CommentRepository commentRepository;

    public WeeklyStatsDto getStatsForClientLast7Days(Client client) {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        int newFollowers = followRelationRepository.countByFollowedAndFollowDateAfter(client, sevenDaysAgo);
        List<Long> followedIds = followRelationRepository.findFollowedClientIds(client);
        int newPosts = postRepository.countByUserIdInAndCreatedAtAfter(followedIds, sevenDaysAgo);
        int newComments = commentRepository.countByUserIdAndCreatedAtAfter(client.getId(), sevenDaysAgo);

        return new WeeklyStatsDto(newFollowers, newPosts, newComments);
    }
}
