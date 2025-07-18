package com.example.OnlyBuns.service;

import com.example.OnlyBuns.dto.ClientDto;
import com.example.OnlyBuns.dto.CommentDto;
import com.example.OnlyBuns.dto.PostDto;
import com.example.OnlyBuns.dto.PostResponseDto;
import com.example.OnlyBuns.model.Client;
import com.example.OnlyBuns.model.Like;
import com.example.OnlyBuns.model.Post;
import com.example.OnlyBuns.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


@Service
public class PostService {
    private final PostRepository postRepository;
    private final LikeService likeService;
    private final String uploadDir = "uploads";
    @Autowired
    private ClientService clientService;


    @Autowired
    public PostService(PostRepository postRepository, LikeService likeService) {
        this.postRepository = postRepository;
        this.likeService = likeService;
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

    @CacheEvict(value = {"tenMostLikedEver", "fiveMostLikedRecently"}, allEntries = true)
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

    public Post findById(Integer id) {
        return postRepository.findById(id).orElse(null);
    }

    @Cacheable(value ="tenMostLikedEver", key = "'mostLiked'")
    public List<Post> findTenMostLikedEver() {
        System.out.println("Fetching from DB");
        List<Post> allPosts = postRepository.findAll();
        return allPosts.stream()
                .sorted((p1, p2) -> Integer.compare(p2.getLikesCount(), p1.getLikesCount())) // Sort descending by likes
                .limit(10)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "fiveMostLikedRecently", key = "'mostLikedRecently'")
    public List<Post> findFiveMostLikedRecently() {
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);

        // Get all likes from the last 7 days
        List<Like> recentLikes = likeService.findAll().stream()
                .filter(like -> like.getLikedAt().isAfter(oneWeekAgo))
                .collect(Collectors.toList());

        // Count likes per postId
        Map<Integer, Long> likeCounts = recentLikes.stream()
                .collect(Collectors.groupingBy(Like::getPostId, Collectors.counting()));

        // Get the top 5 postIds with the most likes in the last 7 days
        List<Integer> topPostIds = likeCounts.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        return topPostIds.stream()
                .map(this::findById)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "imageBytesCache", key = "#relativeImagePath")
    public byte[] getImageBytes(String relativeImagePath) throws IOException {
        String cleanPath = relativeImagePath.startsWith("/uploads/") ? relativeImagePath.substring("/uploads/".length()) : relativeImagePath;

        Path filePath = Paths.get(uploadDir, cleanPath);

        System.out.println(">>> Učitavam sliku sa diska (i keširam u L2): " + filePath.toAbsolutePath());
        return Files.readAllBytes(filePath);
    }

  /*  public List<PostResponseDto> getAllPostsWithComments() {
        List<Post> posts = postRepository.findAll();

        return posts.stream().map(post -> {
            PostResponseDto dto = new PostResponseDto();

            dto.setId(post.getId());
            dto.setUserId(post.getUserId());
            dto.setDescription(post.getDescription());
            dto.setLongitude(post.getLongitude());
            dto.setLatitude(post.getLatitude());
            dto.setImagePath(post.getImagePath());
            dto.setLikesCount(post.getLikesCount());
            dto.setCreatedAt(post.getCreatedAt());

            // Mapiranje komentara
            List<CommentDto> commentDtos = post.getComments().stream().map(comment -> {
                CommentDto commentDto = new CommentDto();
                commentDto.setPostId(post.getId());
                commentDto.setUserId(comment.getUserId());
                commentDto.setContent(comment.getContent());
                commentDto.setCreatedAt(comment.getCreatedAt());

                ClientDto client = clientService.getById(comment.getUserId());
                commentDto.setUsername(client != null ? client.getUsername() : "Nepoznat");

                return commentDto;
            }).collect(Collectors.toList());

            dto.setComments(commentDtos);

            return dto;
        }).collect(Collectors.toList());
    }*/


}
