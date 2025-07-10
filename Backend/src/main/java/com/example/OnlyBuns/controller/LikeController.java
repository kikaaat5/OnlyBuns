package com.example.OnlyBuns.controller;

import com.example.OnlyBuns.model.Like;
import com.example.OnlyBuns.service.LikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/likes")
@CrossOrigin(origins = "http://localhost:4200")
public class LikeController {

    private final LikeService likeService;

    @Autowired
    public LikeController(LikeService likeService) {
        this.likeService = likeService;
    }

    @GetMapping
    public List<Like> getAllLikes() {
        return likeService.findAll();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Like>> getLikesByUserId(@PathVariable int userId) {
        List<Like> likes = likeService.findByUserId(userId);
        return new ResponseEntity<>(likes, HttpStatus.OK);
    }

    @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<Like> createLike(@RequestBody Like like) {
        try {
            Like savedLike = likeService.save(like);
            return new ResponseEntity<>(savedLike, HttpStatus.CREATED); // Status 201 Created
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.CONFLICT); // Status 409 Conflict ako je već lajkovano
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR); // Status 500 za ostale greške
        }
    }
    @DeleteMapping("/{postId}/{userId}")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<String> dislikePost(@PathVariable int postId, @PathVariable int userId) {
        try {
            likeService.dislike(userId, postId);
            return new ResponseEntity<>("Post disliked successfully.", HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("Error disliking post: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR); // Status 500
        }
    }
    @GetMapping("/isLiked/{postId}/{userId}")
    public ResponseEntity<Boolean> isPostLikedByUser(@PathVariable int postId, @PathVariable int userId) {
        boolean liked = likeService.isLiked(userId, postId);
        return new ResponseEntity<>(liked, HttpStatus.OK);
    }
}
