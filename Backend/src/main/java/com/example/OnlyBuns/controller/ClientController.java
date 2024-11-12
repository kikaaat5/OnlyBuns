package com.example.OnlyBuns.controller;

import com.example.OnlyBuns.dto.UserRequest;
import com.example.OnlyBuns.model.Client;
import com.example.OnlyBuns.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
//@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/clients")
public class ClientController {

    private final ClientService clientService;

    @Autowired
    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping
    public List<Client> getAllClients() {
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

    @GetMapping("/search")
    public List<Client> searchClients(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String surname,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Integer minPosts,
            @RequestParam(required = false) Integer maxPosts
    ) {
        if (name != null) {
            return clientService.searchByName(name);
        } else if (surname != null) {
            return clientService.searchBySurname(surname);
        } else if (email != null) {
            return clientService.searchByEmail(email);
        } else if (minPosts != null && maxPosts != null) {
            return clientService.searchByNumberOfPostsInRange(minPosts, maxPosts);
        }
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
}
