package com.example.OnlyBuns.controller;

import com.example.OnlyBuns.model.Like;
import com.example.OnlyBuns.service.LikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/likes")
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
