package com.example.OnlyBuns.controller;

import com.example.OnlyBuns.model.Like;
import com.example.OnlyBuns.model.Post;
import com.example.OnlyBuns.service.LikeService;
import com.example.OnlyBuns.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/posts")
@CrossOrigin(origins = "http://localhost:4200")
public class PostController {

    private final PostService postService;
    private final LikeService likeService;
    private static final Logger logger = LoggerFactory.getLogger(PostController.class);

    @Autowired
    public PostController(PostService postService, LikeService likeService) {
        this.postService = postService;
        this.likeService = likeService;
    }

    @GetMapping
    public List<Post> getAllPosts() {
        return postService.findAll();
    }

    @PostMapping
    public Post createPost(@RequestBody Post post) {
        logger.debug("create metoda na serveru");

        return postService.save(post);
    }

    @DeleteMapping("/{postId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT')")
    public ResponseEntity<Void> deletePost(@PathVariable int postId, @RequestParam int userId) {
        postService.deletePost(postId, userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT')")
    public Post updatePost(@PathVariable int id, @RequestBody Post updatedPost, @RequestParam int userId) {
        return postService.updatePost(id, updatedPost, userId);

    }

    @GetMapping("/posts/{userId}")
    public List<Post> findPostsByUserId(@PathVariable int userId) {
        return postService.findPostsByUserId(userId);
    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<Void> likePost(@PathVariable int postId) {
        postService.likePost(postId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/posts/tenMostLikedEver")
    public List<Post> findTenMostLikedEver() {
        List<Post> allPosts = postService.findAll();

        return allPosts.stream()
                .sorted((p1, p2) -> Integer.compare(p2.getLikesCount(), p1.getLikesCount())) // Sort descending by likes
                .limit(10) // Get top 10
                .collect(Collectors.toList());
    }


    @GetMapping("/posts/fiveLastWeeksMostLiked")
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
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue())) // Sort descending by like count
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // Fetch and return the corresponding posts
        return topPostIds.stream()
                .map(postService::findById)
                .filter(Objects::nonNull) // Ensure only existing posts are included
                .collect(Collectors.toList());
    }

}
