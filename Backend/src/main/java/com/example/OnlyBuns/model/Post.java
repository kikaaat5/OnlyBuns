package com.example.OnlyBuns.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private int userId;
    private String description;

    private LocalDateTime createdAt;
    private String imagePath;
    private double longitude;
    private double latitude;
    private int likesCount;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments;

    public Post() {
        this.createdAt = LocalDateTime.now();
    }
}
