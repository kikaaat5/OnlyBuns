package com.example.OnlyBuns.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private int id;

    @Getter
    private int userId;

    @Getter @Setter
    private String description;

    private LocalDateTime createdAt;

    @Getter @Setter
    private String imagePath;

    @Getter @Setter
    private double longitude;

    @Getter @Setter
    private double latitude;

    @Getter @Setter
    private int likesCount;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments;

    public Post() {
        this.createdAt = LocalDateTime.now();
    }


}
