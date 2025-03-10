package com.example.OnlyBuns.controller;

import com.example.OnlyBuns.model.Like;
import com.example.OnlyBuns.service.LikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @PostMapping
    public Like createLike(@RequestBody Like like) {
        return likeService.save(like);
    }
}
