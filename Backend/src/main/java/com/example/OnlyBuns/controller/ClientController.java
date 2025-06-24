package com.example.OnlyBuns.controller;

import com.example.OnlyBuns.dto.ClientDto;
import com.example.OnlyBuns.dto.AddressDto;
import com.example.OnlyBuns.dto.UserRequest;
import com.example.OnlyBuns.model.Client;
import com.example.OnlyBuns.model.Like;
import com.example.OnlyBuns.model.Role;
import com.example.OnlyBuns.model.User;
import com.example.OnlyBuns.service.ClientService;
import com.example.OnlyBuns.service.LikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
//@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/clients")
public class ClientController {

    private final ClientService clientService;
    private final LikeService likeService;

    @Autowired
    public ClientController(ClientService clientService, LikeService likeService) {
        this.clientService = clientService;
        this.likeService = likeService;
    }

    @GetMapping
    public List<ClientDto> getAllClients() {
        return clientService.findAll();
    }

    @PostMapping
    public Client createClient(@RequestBody UserRequest client) {
        return clientService.save(client);
    }

    @DeleteMapping("/{id}")
    public void deleteClient(@PathVariable int id) {
        clientService.deleteById(id);
    }

    @GetMapping("/{id}")
    public ClientDto getClient(@PathVariable int id) {
        return clientService.getById(id);
    }

    @GetMapping("/search")
    public List<ClientDto> searchClients(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String surname,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Integer minPosts,
            @RequestParam(required = false) Integer maxPosts
    ) {
        /*if (name != null) {
            return clientService.searchByName(name);
        } else if (surname != null) {
            return clientService.searchBySurname(surname);
        } else if (email != null) {
            return clientService.searchByEmail(email);
        } else if (minPosts != null && maxPosts != null) {
            return clientService.searchByNumberOfPostsInRange(minPosts, maxPosts);
        }*/
        return clientService.findAll();
    }

    @GetMapping("/sort/followingCount")
    public List<Client> sortClientsByFollowingCount() {
        return clientService.sortByFollowingCount();
    }

    @GetMapping("/sort/email")
    public List<Client> sortClientsByEmail() {
        return clientService.sortByEmail();
    }

    @GetMapping("/mostActiveClients")
    public List<ClientDto> getMostActiveClients() {
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);

        // Get all likes from the last 7 days
        List<Like> recentLikes = likeService.findAll().stream()
                .filter(like -> like.getLikedAt().isAfter(oneWeekAgo))
                .collect(Collectors.toList());

        // Count likes per user_id
        Map<Integer, Long> likeCounts = recentLikes.stream()
                .collect(Collectors.groupingBy(Like::getUserId, Collectors.counting()));

        // Get top 10 userIds sorted by like count
        List<Integer> topUserIds = likeCounts.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue())) // Sort descending by like count
                .limit(10)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // Fetch and return the corresponding clients
        return topUserIds.stream()
                .map(clientService::getById) // Assuming you have clientService.findById(userId)
                .filter(Objects::nonNull) // Ensure only existing clients are included
                .collect(Collectors.toList());
    }

}
