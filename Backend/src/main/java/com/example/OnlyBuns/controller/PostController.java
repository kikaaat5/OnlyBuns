package com.example.OnlyBuns.controller;

import com.example.OnlyBuns.model.Post;
import com.example.OnlyBuns.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@CrossOrigin(origins = "http://localhost:4200")
public class PostController {

    private final PostService postService;
    private static final Logger logger = LoggerFactory.getLogger(PostController.class);

    @Autowired
    public PostController(PostService postService) {
        this.postService = postService;
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

}
