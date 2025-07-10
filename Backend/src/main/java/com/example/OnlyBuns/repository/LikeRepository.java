package com.example.OnlyBuns.repository;

import com.example.OnlyBuns.model.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Integer>{
    Optional<Like> findByUserIdAndPostId(int userId, int postId);

    List<Like> findByUserId(int userId);

    @Transactional
    void deleteByUserIdAndPostId(int userId, int postId);
}