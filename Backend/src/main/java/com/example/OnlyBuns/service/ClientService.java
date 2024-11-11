package com.example.OnlyBuns.service;

import com.example.OnlyBuns.repository.ClientRepository;
import com.example.OnlyBuns.model.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ClientService {
    private final ClientRepository clientRepository;

    @Autowired
    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public List<Client> findAll() {
        return clientRepository.findAll();
    }

    public Client save(Client client) {
        return clientRepository.save(client);
    }

    public void deleteById(int id) {
        clientRepository.deleteById(id);
    }

    public List<Client> searchByName(String name) {
        return clientRepository.findByNameContainingIgnoreCase(name);
    }

    public List<Client> searchBySurname(String surname) {
        return clientRepository.findBySurnameContainingIgnoreCase(surname);
    }

    public List<Client> searchByEmail(String email) {
        return clientRepository.findByEmailContainingIgnoreCase(email);
    }

    public List<Client> searchByNumberOfPostsInRange(int minPosts, int maxPosts) {
        return clientRepository.findByNumberOfPostsInRange(minPosts, maxPosts);
    }

    public List<Client> sortByFollowingCount() {
        return clientRepository.findAllByOrderByFollowingDesc();
    }

    public List<Client> sortByEmail() {
        return clientRepository.findAllByOrderByEmailAsc();
    }
}
