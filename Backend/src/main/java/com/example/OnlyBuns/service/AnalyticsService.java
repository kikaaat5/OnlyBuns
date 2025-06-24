package com.example.OnlyBuns.service;

import com.example.OnlyBuns.repository.PostRepository;
import com.example.OnlyBuns.repository.CommentRepository;
import com.example.OnlyBuns.repository.ClientRepository;
import com.example.OnlyBuns.dto.PostCommentStatsDto;
import com.example.OnlyBuns.dto.ClientActivityStatsDto;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class AnalyticsService {
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final ClientRepository clientRepository;

    public AnalyticsService(PostRepository postRepository, CommentRepository commentRepository, ClientRepository clientRepository) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.clientRepository = clientRepository;
    }

    public PostCommentStatsDto getPostCommentStatistics() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneWeekAgo = now.minus(1, ChronoUnit.WEEKS);
        LocalDateTime oneMonthAgo = now.minus(1, ChronoUnit.MONTHS);
        LocalDateTime oneYearAgo = now.minus(1, ChronoUnit.YEARS);

        long weeklyPosts = postRepository.countByCreatedAtAfter(oneWeekAgo);
        long monthlyPosts = postRepository.countByCreatedAtAfter(oneMonthAgo);
        long yearlyPosts = postRepository.countByCreatedAtAfter(oneYearAgo);

        long weeklyComments = commentRepository.countByCreatedAtAfter(oneWeekAgo);
        long monthlyComments = commentRepository.countByCreatedAtAfter(oneMonthAgo);
        long yearlyComments = commentRepository.countByCreatedAtAfter(oneYearAgo);

        PostCommentStatsDto stats = new PostCommentStatsDto();
        stats.setWeeklyPosts(weeklyPosts);
        stats.setMonthlyPosts(monthlyPosts);
        stats.setYearlyPosts(yearlyPosts);
        stats.setWeeklyComments(weeklyComments);
        stats.setMonthlyComments(monthlyComments);
        stats.setYearlyComments(yearlyComments);

        return stats;
    }

    public ClientActivityStatsDto getClientActivityStatistics() {

        long totalClients = clientRepository.count();
        long clientsWithPosts = clientRepository.countClientsWithPosts();
        long clientsWithOnlyComments = clientRepository.countClientsWithOnlyComments();

        long activeClients = clientsWithPosts + clientsWithOnlyComments;
        long inactiveClients = totalClients - activeClients;

        if (totalClients == 0) {
            return new ClientActivityStatsDto();
        }

        ClientActivityStatsDto stats = new ClientActivityStatsDto();
        stats.setClientsWithPostsPercentage((double) clientsWithPosts / totalClients * 100);
        stats.setClientsWithOnlyCommentsPercentage((double) clientsWithOnlyComments / totalClients * 100);
        stats.setInactiveClientsPercentage((double) inactiveClients / totalClients * 100);

        return stats;
    }
}