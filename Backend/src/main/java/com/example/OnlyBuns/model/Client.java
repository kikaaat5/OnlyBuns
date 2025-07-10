package com.example.OnlyBuns.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.List;

@Entity
@Table(name="client")
public class Client extends User {
    @Column(name="numberOfPosts", unique=false, nullable=true)
    private int numberOfPosts;
    @Column(name="following", unique=false, nullable=true)
    private int following;
    @Column(name="active", unique=false, nullable=true)
    private boolean active;
    @Column(name="followers", unique=false, nullable=true)
    private int followers;
    //public List<User> followers;
    //public List<Post> posts;


    public Client(Integer id, String email, String username, String password, String name, String surname, int numberOfPosts, int following, boolean active) {
        super();
        this.numberOfPosts = numberOfPosts;
        this.following = following;
        this.active = active;
    }

    public Client() {
    }

    public int getNumberOfPosts() {
        return numberOfPosts;
    }

    public void setNumberOfPosts(int numberOfPosts) {
        this.numberOfPosts = numberOfPosts;
    }

    public int getFollowing() {
        return following;
    }

    public void setFollowing(int following) {
        this.following = following;
    }
    public int getFollowers() {
        return followers;
    }

    public void setFollowers(int followers) {
        this.followers = followers;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}

