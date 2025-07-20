package com.example.OnlyBuns.repository;

import com.example.OnlyBuns.model.Client;
import com.example.OnlyBuns.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer> {

    long countByCreatedAtAfter(LocalDateTime createdAt);
    List<Comment> findByPostIdOrderByCreatedAtDesc(int postId);
    int countByUserIdAndCreatedAtAfter(long userId, LocalDateTime date);

}
