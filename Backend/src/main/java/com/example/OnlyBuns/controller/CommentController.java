package com.example.OnlyBuns.controller;

import com.example.OnlyBuns.dto.CommentDto;
import com.example.OnlyBuns.model.Comment;
import com.example.OnlyBuns.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @PostMapping
    public ResponseEntity<String> addComment(@RequestBody CommentDto commentDto) {
        commentService.addComment(commentDto);
        return ResponseEntity.ok("Komentar uspešno dodat.");
    }

    @GetMapping("/post/{postId}")
    public List<CommentDto> getComments(@PathVariable int postId) {
        return commentService.getCommentsForPost(postId);
    }

    @DeleteMapping("/{id}")
    public void deleteComment(@PathVariable int id) {
        commentService.deleteById(id); // Ova metoda je ok ako je koristiš za admina
    }
}

