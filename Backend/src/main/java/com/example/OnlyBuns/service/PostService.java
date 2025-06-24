package com.example.OnlyBuns.service;

import com.example.OnlyBuns.dto.PostDto;
import com.example.OnlyBuns.model.Post;
import com.example.OnlyBuns.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


@Service
public class PostService {
    private final PostRepository postRepository;
    private final String uploadDir = "uploads";


    @Autowired
    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public Post createPost(PostDto dto) throws IOException {
        // Sačuvaj sliku
        MultipartFile image = dto.getImage();
        String uploadDir = "uploads";
        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs();

        String extension = image.getOriginalFilename().substring(image.getOriginalFilename().lastIndexOf("."));
        String uniqueFileName = UUID.randomUUID().toString() + extension;
        Path filePath = Paths.get(uploadDir, uniqueFileName);
        Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        String imagePath = "/uploads/" + uniqueFileName;

        // Kreiraj post
        Post post = new Post();
        post.setUserId(dto.getUserId());
        post.setDescription(dto.getDescription());
        post.setLatitude(dto.getLatitude());
        post.setLongitude(dto.getLongitude());
        post.setImagePath(imagePath);
        post.setCreatedAt(LocalDateTime.now());

        return postRepository.save(post);
    }

    public List<Post> findAll() {
        return postRepository.findAll();
    }

    /*public Post save(Post post) {
        return postRepository.save(post);
    }*/

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
