package com.example.OnlyBuns.service;

import com.example.OnlyBuns.model.Like;
import com.example.OnlyBuns.model.Post;
import com.example.OnlyBuns.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class PostService {
    private final PostRepository postRepository;
    private final LikeService likeService;

    @Autowired
    public PostService(PostRepository postRepository, LikeService likeService) {
        this.postRepository = postRepository;
        this.likeService = likeService;
    }

    public List<Post> findAll() {
        return postRepository.findAll();
    }

    public Post save(Post post) {
        return postRepository.save(post);
    }

    public void deleteById(int id) {
        postRepository.deleteById(id);
    }

    public void deletePost(int postId, int userId) {
        Post existingPost = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (existingPost.getUserId() != userId) {
            throw new RuntimeException("You can only delete your own posts");
        }

        postRepository.delete(existingPost);
    }

    public Post updatePost(int postId, Post updatedPost, int userId) {

        Post existingPost = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (existingPost.getUserId() != userId) {
            throw new RuntimeException("You can only edit your own posts");
        }
        existingPost.setDescription(updatedPost.getDescription());
        existingPost.setImagePath(updatedPost.getImagePath());
        existingPost.setLongitude(updatedPost.getLongitude());
        existingPost.setLatitude(updatedPost.getLatitude());

        return postRepository.save(existingPost);
    }

    public List<Post> findPostsByUserId(int userId) {
        return postRepository.findAllByUserId(userId);
    }

    @CacheEvict(value = {"tenMostLikedEver", "fiveMostLikedRecently"}, allEntries = true)
    public void likePost(int postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        post.setLikesCount(post.getLikesCount() + 1);
        postRepository.save(post);
    }

    public Post findById(Integer id) {
        return postRepository.findById(id).orElse(null);
    }

    @Cacheable("tenMostLikedEver")
    public List<Post> findTenMostLikedEver() {
        List<Post> allPosts = postRepository.findAll();
        return allPosts.stream()
                .sorted((p1, p2) -> Integer.compare(p2.getLikesCount(), p1.getLikesCount())) // Sort descending by likes
                .limit(10)
                .collect(Collectors.toList());
    }

    @Cacheable("fiveMostLikedRecently")
    public List<Post> findFiveMostLikedRecently() {
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);

        // Get all likes from the last 7 days
        List<Like> recentLikes = likeService.findAll().stream()
                .filter(like -> like.getLikedAt().isAfter(oneWeekAgo))
                .collect(Collectors.toList());

        // Count likes per postId
        Map<Integer, Long> likeCounts = recentLikes.stream()
                .collect(Collectors.groupingBy(Like::getPostId, Collectors.counting()));

        // Get the top 5 postIds with the most likes in the last 7 days
        List<Integer> topPostIds = likeCounts.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        return topPostIds.stream()
                .map(this::findById)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

}
