package com.example.OnlyBuns.repository;

import com.example.OnlyBuns.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Integer>{

    List<Post> findAllByUserId(int userId);
}
