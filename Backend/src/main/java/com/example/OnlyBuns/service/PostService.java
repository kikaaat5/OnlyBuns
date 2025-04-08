package com.example.OnlyBuns.service;

import com.example.OnlyBuns.model.Post;
import com.example.OnlyBuns.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


@Service
public class PostService {
    private final PostRepository postRepository;

    @Autowired
    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public List<Post> findAll() {
        return postRepository.findAll();
    }

    public Post save(Post post) {
        return postRepository.save(post);
    }

    public void deleteById(int id) {
        postRepository.deleteById(id);
    }

    public void deletePost(int postId, int userId) {
        Post existingPost = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (existingPost.getUserId() != userId) {
            throw new RuntimeException("You can only delete your own posts");
        }

        postRepository.delete(existingPost);
    }

    public Post updatePost(int postId, Post updatedPost, int userId) {

        Post existingPost = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (existingPost.getUserId() != userId) {
            throw new RuntimeException("You can only edit your own posts");
        }
        existingPost.setDescription(updatedPost.getDescription());
        existingPost.setImagePath(updatedPost.getImagePath());
        existingPost.setLongitude(updatedPost.getLongitude());
        existingPost.setLatitude(updatedPost.getLatitude());

        return postRepository.save(existingPost);
    }

    public List<Post> findPostsByUserId(int userId) {
        return postRepository.findAllByUserId(userId);
    }

    public void likePost(int postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        post.setLikesCount(post.getLikesCount() + 1);
        postRepository.save(post);
    }

    public Post findOne(int id) {
        System.out.println(">>> Pozivam findById za id: " + id);
        return postRepository.findById(id).orElseThrow(() -> new RuntimeException("Post not found"));
    }



}
