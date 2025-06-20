package com.example.OnlyBuns.controller;

import com.example.OnlyBuns.dto.ClientDto;
import com.example.OnlyBuns.model.Client;
import com.example.OnlyBuns.service.ClientService;
import com.example.OnlyBuns.service.FollowRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/follows")
@CrossOrigin(origins = "http://localhost:4200")
public class FollowRelationController {

    private final FollowRelationService followService;
    private final ClientService clientService;

    @Autowired
    public FollowRelationController(FollowRelationService followService, ClientService clientService) {
        this.followService = followService;
        this.clientService = clientService;
    }

    private int getCurrentClientId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new IllegalStateException("Korisnik nije autentifikovan. Prijavite se da biste izvršili ovu operaciju.");
        }

        String username = authentication.getName();

        Client client = clientService.findByUsername(username);

        if (client != null) {

            return client.getId().intValue();
        } else {
            throw new IllegalStateException("Ulogovani klijent sa emailom '" + username + "' nije pronađen u bazi podataka.");
        }
    }

    @PostMapping("/{followedClientId}/follow")
    public ResponseEntity<String> followClient(@PathVariable int followedClientId) {
        try {
            int currentFollowerId = getCurrentClientId();

            String message = followService.followClient(currentFollowerId, followedClientId);
            return new ResponseEntity<>(message, HttpStatus.CREATED); // Status 201 Created
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED); // Status 401 Unauthorized ili 403 Forbidden
        } catch (IllegalArgumentException e) { // Npr. ako klijent ne postoji ili već prati
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST); // Status 400 Bad Request
        } catch (Exception e) {
            return new ResponseEntity<>("Došlo je do interne greške: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR); // Status 500
        }
    }

    @DeleteMapping("/{followedClientId}/unfollow")
    public ResponseEntity<String> unfollowClient(@PathVariable int followedClientId) {
        try {
            int currentFollowerId = getCurrentClientId();

            String message = followService.unfollowClient(currentFollowerId, followedClientId);
            return new ResponseEntity<>(message, HttpStatus.OK); // Status 200 OK
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
        } catch (IllegalArgumentException e) { // Npr. ako klijent ne postoji ili ne prati
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("Došlo je do interne greške: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("/{otherClientId}/isFollowing")
    public ResponseEntity<Boolean> isFollowing(@PathVariable int otherClientId) {
        try {
            int currentFollowerId = getCurrentClientId();

            boolean isFollowing = followService.isFollowing(currentFollowerId, otherClientId);
            return new ResponseEntity<>(isFollowing, HttpStatus.OK); // Status 200 OK
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(false, HttpStatus.UNAUTHORIZED);
        } catch (IllegalArgumentException e) { // Npr. ako klijent ne postoji
            return new ResponseEntity<>(false, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{clientId}/following")
    public ResponseEntity<List<ClientDto>> getFollowing(@PathVariable int clientId) {
        try {
            List<ClientDto> followingClients = followService.getFollowing(clientId); // Prosleđuje int ID
            // Proveri da li je lista prazna ili null i vrati 404 ako klijent ne postoji
            if (followingClients == null || followingClients.isEmpty()) { // Ovo pretpostavlja da FollowService može vratiti null ili praznu listu ako klijent ne postoji
                // Bolje je da FollowService baci ResourceNotFoundException za nepostojeći klijent
                return new ResponseEntity<>(HttpStatus.NOT_FOUND); // Status 404 Not Found
            }
            return new ResponseEntity<>(followingClients, HttpStatus.OK); // Status 200 OK
        } catch (IllegalArgumentException e) { // Ako klijent sa tim ID-em ne postoji u servisu
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // Status 404 Not Found
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{clientId}/followers")
    public ResponseEntity<List<ClientDto>> getFollowers(@PathVariable int clientId) {
        try {
            List<ClientDto> followersClients = followService.getFollowers(clientId); // Prosleđuje int ID
            if (followersClients == null || followersClients.isEmpty()) { // Isto kao gore
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(followersClients, HttpStatus.OK); // Status 200 OK
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{clientId}/following/count")
    public ResponseEntity<Long> getFollowingCount(@PathVariable int clientId) {
        try {
            // Broj praćenja se u bazi može izraziti kao Long, pa je povratna vrednost Long
            long count = followService.getFollowingCount(clientId); // Prosleđuje int ID
            return new ResponseEntity<>(count, HttpStatus.OK); // Status 200 OK
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(0L, HttpStatus.NOT_FOUND); // Status 404 Not Found
        } catch (Exception e) {
            return new ResponseEntity<>(0L, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{clientId}/followers/count")
    public ResponseEntity<Long> getFollowersCount(@PathVariable int clientId) {
        try {
            // Broj pratilaca se u bazi može izraziti kao Long, pa je povratna vrednost Long
            long count = followService.getFollowersCount(clientId); // Prosleđuje int ID
            return new ResponseEntity<>(count, HttpStatus.OK); // Status 200 OK
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(0L, HttpStatus.NOT_FOUND); // Status 404 Not Found
        } catch (Exception e) {
            return new ResponseEntity<>(0L, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}