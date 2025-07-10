package com.example.OnlyBuns.service;

import com.example.OnlyBuns.model.Like;
import com.example.OnlyBuns.model.Post;
import com.example.OnlyBuns.repository.LikeRepository;
import com.example.OnlyBuns.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class LikeService {
    private final LikeRepository likeRepository;
    private final PostRepository postRepository;

    @Autowired
    public LikeService(LikeRepository likeRepository, PostRepository postRepository) {
        this.likeRepository = likeRepository;
        this.postRepository = postRepository;
    }

    public List<Like> findAll() {
        return likeRepository.findAll();
    }

    public List<Like> findByUserId(int userId) {
        return likeRepository.findByUserId(userId);
    }
    @Transactional
    public Like save(Like like) {
        Optional<Like> existingLike = likeRepository.findByUserIdAndPostId(like.getUserId(), like.getPostId());
        if (existingLike.isPresent()) {
            throw new IllegalArgumentException("User has already liked this post.");
        }
        Like savedLike = likeRepository.save(like);

        Post post = postRepository.findById(like.getPostId())
                .orElseThrow(() -> new IllegalArgumentException("Post not found with ID: " + like.getPostId()));
        post.setLikesCount(post.getLikesCount() + 1);
        postRepository.save(post);

        return savedLike;
    }
    @Transactional
    public void dislike(int userId, int postId) {
        Optional<Like> existingLike = likeRepository.findByUserIdAndPostId(userId, postId);
        if (existingLike.isEmpty()) {
            throw new IllegalArgumentException("User has not liked this post, cannot dislike.");
        }

        likeRepository.deleteByUserIdAndPostId(userId, postId);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found with ID: " + postId));
        if (post.getLikesCount() > 0) {
            post.setLikesCount(post.getLikesCount() - 1);
            postRepository.save(post);
        }
    }

    public boolean isLiked(int userId, int postId) {
        return likeRepository.findByUserIdAndPostId(userId, postId).isPresent();
    }
}
