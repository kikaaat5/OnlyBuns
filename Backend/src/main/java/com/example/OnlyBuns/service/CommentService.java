package com.example.OnlyBuns.service;

import com.example.OnlyBuns.dto.CommentDto;
import com.example.OnlyBuns.model.Client;
import com.example.OnlyBuns.model.Comment;
import com.example.OnlyBuns.model.Post;
import com.example.OnlyBuns.repository.ClientRepository;
import com.example.OnlyBuns.repository.CommentRepository;
import com.example.OnlyBuns.repository.FollowRelationRepository;
import com.example.OnlyBuns.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CommentService {
    private final CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private FollowRelationRepository followRelationRepository;

    @Autowired
    private CommentRateLimiterService rateLimiter;

    @Autowired
    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    public List<Comment> findAll() {
        return commentRepository.findAll();
    }

    public Comment save(Comment comment) {
        return commentRepository.save(comment);
    }

    public void deleteById(int id) {
        commentRepository.deleteById(id);
    }


    public void addComment(CommentDto dto) {

        Optional<Client> authorOpt = clientRepository.findById( dto.getUserId());
        Optional<Post> postOpt = postRepository.findById(dto.getPostId());

        if (!rateLimiter.canComment(dto.getUserId())) {
            throw new RuntimeException("Prekoračen limit: Maksimalno 60 komentara po satu.");
        }
        if (authorOpt.isEmpty() || postOpt.isEmpty()) {
            throw new RuntimeException("Nevalidan korisnik ili post.");
        }

        Client author = authorOpt.get();
        Post post = postOpt.get();

        // Dohvati vlasnika posta
        Client postOwner = clientRepository.findById( post.getUserId())
                .orElseThrow(() -> new RuntimeException("Vlasnik objave ne postoji."));

        // Provera da li korisnik prati vlasnika posta
        boolean follows = followRelationRepository.findByFollowerAndFollowed(author, postOwner).isPresent();
        if (!follows) {
            throw new RuntimeException("Ne možete komentarisati objave korisnika koje ne pratite.");
        }

        Comment comment = new Comment();
        comment.setContent(dto.getContent());
        comment.setPost(post);
        comment.setUserId(dto.getUserId());
        comment.setCreatedAt(LocalDateTime.now());

        commentRepository.save(comment);
    }

        public List<CommentDto> getCommentsForPost(int postId) {
        List<Comment> comments = commentRepository.findByPostIdOrderByCreatedAtDesc(postId);

        return comments.stream().map(comment -> {
            String username = clientRepository.findById(comment.getUserId())
                    .map(Client::getUsername) // pretpostavljam da Client ima getUsername()
                    .orElse("Nepoznat");

            return new CommentDto(
                    postId,
                    comment.getUserId(),
                    username,
                    comment.getContent(),
                    comment.getCreatedAt()
            );
        }).collect(Collectors.toList());
    }


}

