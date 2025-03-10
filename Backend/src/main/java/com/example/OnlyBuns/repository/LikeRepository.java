package com.example.OnlyBuns.repository;

import com.example.OnlyBuns.model.Like;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LikeRepository extends JpaRepository<Like, Integer>{
}