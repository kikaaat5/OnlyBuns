package com.example.OnlyBuns.repository;

import com.example.OnlyBuns.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Integer> {
}
