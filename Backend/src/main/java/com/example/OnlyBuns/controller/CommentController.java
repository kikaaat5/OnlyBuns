package com.example.OnlyBuns.controller;

import com.example.OnlyBuns.model.Comment;
import com.example.OnlyBuns.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;

    @Autowired
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    public List<Comment> getAllComments() {
        return commentService.findAll();
    }

    @PostMapping
    public Comment createComment(@RequestBody Comment comment) {
        return commentService.save(comment);
    }

    @DeleteMapping("/{id}")
    public void deleteComment(@PathVariable int id) {
        commentService.deleteById(id);
    }
}
