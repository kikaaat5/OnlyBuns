package com.example.OnlyBuns.dto;

public class WeeklyStatsDto {
    private int newFollowers;
    private int newPostsFromFollowed;
    private int newComments;

    public WeeklyStatsDto(int newFollowers, int newPostsFromFollowed, int newComments) {
        this.newFollowers = newFollowers;
        this.newPostsFromFollowed = newPostsFromFollowed;
        this.newComments = newComments;
    }

    public int getNewFollowers() {
        return newFollowers;
    }

    public int getNewPostsFromFollowed() {
        return newPostsFromFollowed;
    }

    public int getNewComments() {
        return newComments;
    }
}