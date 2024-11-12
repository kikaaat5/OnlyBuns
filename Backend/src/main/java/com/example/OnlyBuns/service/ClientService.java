package com.example.OnlyBuns.service;

import com.example.OnlyBuns.dto.UserRequest;
import com.example.OnlyBuns.model.Role;
import com.example.OnlyBuns.model.User;
import com.example.OnlyBuns.repository.ClientRepository;
import com.example.OnlyBuns.model.Client;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Service
@Transactional
public class ClientService {
    private final ClientRepository clientRepository;

    @Autowired
    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleService roleService;

    public List<Client> findAll() {
        return clientRepository.findAll();
    }

    public Client save(UserRequest userRequest) {
        Client c = new Client();
        c.setUsername(userRequest.getUsername());

        // pre nego sto postavimo lozinku u atribut hesiramo je kako bi se u bazi nalazila hesirana lozinka
        // treba voditi racuna da se koristi isi password encoder bean koji je postavljen u AUthenticationManager-u kako bi koristili isti algoritam
        c.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        c.setFirstName(userRequest.getFirstname());
        c.setLastName(userRequest.getLastname());
        c.setEnabled(true);
        c.setEmail(userRequest.getEmail());
        c.setActive(true);
        c.setFollowing(0);
        c.setNumberOfPosts(0);
        List<Role> roles = roleService.findByName("ROLE_CLIENT");
        c.setRoles(roles);
        return this.clientRepository.save(c);
    }

    public void deleteById(int id) {
        clientRepository.deleteById(id);
    }

    public List<Client> searchByName(String name) {
        return clientRepository.findByFirstnameContainingIgnoreCase(name);
    }

    public List<Client> searchBySurname(String surname) {
        return clientRepository.findByLastnameContainingIgnoreCase(surname);
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
