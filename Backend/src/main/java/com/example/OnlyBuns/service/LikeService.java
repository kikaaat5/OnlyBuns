package com.example.OnlyBuns.service;

import com.example.OnlyBuns.model.Like;
import com.example.OnlyBuns.repository.LikeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LikeService {
    private final LikeRepository likeRepository;

    @Autowired
    public LikeService(LikeRepository likeRepository) {
        this.likeRepository = likeRepository;
    }

    public List<Like> findAll() {
        return likeRepository.findAll();
    }

    public Like save(Like like) {
        return likeRepository.save(like);
    }
}
