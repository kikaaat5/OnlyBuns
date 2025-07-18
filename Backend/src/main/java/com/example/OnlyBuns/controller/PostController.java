package com.example.OnlyBuns.controller;

import com.example.OnlyBuns.dto.PostResponseDto;
import com.example.OnlyBuns.model.Like;
import com.example.OnlyBuns.dto.PostDto;
import com.example.OnlyBuns.model.Post;
import com.example.OnlyBuns.service.LikeService;
import com.example.OnlyBuns.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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

//    @GetMapping("/comments")
//    public ResponseEntity<List<PostResponseDto>> getAllPostsWithComments() {
//        return ResponseEntity.ok(postService.getAllPostsWithComments());
//    }

    @PostMapping("/create")
    public ResponseEntity<?> createPost(@ModelAttribute PostDto dto) {
        try {
            Post post = postService.createPost(dto);
            return ResponseEntity.ok(post);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Greška pri snimanju posta");
        }
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

    @GetMapping("/test-cache/{id}")
    public ResponseEntity<Post> testCache(@PathVariable int id) {
        System.out.println(">>> Pozivam testCache za post id: " + id);
        Post post = postService.findOne(id);
        return ResponseEntity.ok(post);
    }


    @PostMapping("/{postId}/like")
    public ResponseEntity<Void> likePost(@PathVariable int postId) {
        postService.likePost(postId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/posts/tenMostLikedEver")
    public List<Post> findTenMostLikedEver() {
        return postService.findTenMostLikedEver();
    }

    @GetMapping("/posts/fiveLastWeeksMostLiked")
    public List<Post> findFiveMostLikedRecently() {
        return postService.findFiveMostLikedRecently();
    }

}
