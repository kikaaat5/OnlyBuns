package com.example.OnlyBuns.controller;

import com.example.OnlyBuns.model.Client;
import com.example.OnlyBuns.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
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
    public Client createClient(@RequestBody Client client) {
        return clientService.save(client);
    }

    @DeleteMapping("/{id}")
    public void deleteClient(@PathVariable int id) {
        clientService.deleteById(id);
    }
}
