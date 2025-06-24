package com.example.OnlyBuns.repository;

import com.example.OnlyBuns.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.time.LocalDateTime;

@Repository
public interface PostRepository extends JpaRepository<Post, Integer>{

    List<Post> findAllByUserId(int userId);

    long countByCreatedAtAfter(LocalDateTime createdAt);
}
