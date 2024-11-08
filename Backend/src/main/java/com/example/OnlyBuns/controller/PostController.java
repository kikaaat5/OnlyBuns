package com.example.OnlyBuns.controller;

import com.example.OnlyBuns.model.Post;
import com.example.OnlyBuns.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

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
        return postService.save(post);
    }

    @DeleteMapping("/{id}")
    public void deletePost(@PathVariable int id) {
        postService.deleteById(id);
    }
}
