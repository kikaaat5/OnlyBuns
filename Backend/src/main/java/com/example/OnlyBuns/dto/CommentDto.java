package com.example.OnlyBuns.dto;

import java.time.LocalDateTime;

public class CommentDto {
    private int postId;
    private int userId;
    private String username; // za prikaz imena autora
    private String content;
    private LocalDateTime createdAt;

    public CommentDto() {}

    // Za kreiranje (unos komentara)
    public CommentDto(int postId, int userId, String content) {
        this.postId = postId;
        this.userId = userId;
        this.content = content;
    }

    // Za prikaz (response)
    public CommentDto(int postId, int userId, String username, String content, LocalDateTime createdAt) {
        this.postId = postId;
        this.userId = userId;
        this.username = username;
        this.content = content;
        this.createdAt = createdAt;
    }
    public int getPostId() {
        return postId;
    }

    public void setPostId(int postId) {
        this.postId = postId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
