package com.example.OnlyBuns.service;

import ch.qos.logback.core.net.SyslogOutputStream;
import com.example.OnlyBuns.dto.UserRequest;
import com.example.OnlyBuns.model.Role;
import com.example.OnlyBuns.model.User;
import com.example.OnlyBuns.repository.ClientRepository;
import com.example.OnlyBuns.model.Client;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

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

    public void save(Client client){clientRepository.save(client);}

    public Client save(UserRequest userRequest) {
        Client c = new Client();
        c.setUsername(userRequest.getUsername());

        // pre nego sto postavimo lozinku u atribut hesiramo je kako bi se u bazi nalazila hesirana lozinka
        // treba voditi racuna da se koristi isi password encoder bean koji je postavljen u AUthenticationManager-u kako bi koristili isti algoritam
        c.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        c.setFirstName(userRequest.getFirstname());
        c.setLastName(userRequest.getLastname());
        c.setEmail(userRequest.getEmail());
        c.setFollowing(0);
        c.setNumberOfPosts(0);
        List<Role> roles = roleService.findByName("ROLE_CLIENT");
        c.setRoles(roles);
        return this.clientRepository.save(c);
    }

    public boolean activateUser(String email) {
        Client client = clientRepository.findByEmail(email);
        if (client != null) {
            client.setActive(true); // Postavlja korisnika kao aktivnog
            clientRepository.save(client);
            return true;// Spasavanje u bazi
        }
        return false;
    }

    public Client findByEmail(String email) {
        return clientRepository.findByEmail(email);
    }

    public void deleteById(int id) {
        clientRepository.deleteById(id);
    }

    public ResponseEntity<String> activate(String username) {
        System.out.println("usao u aktivacionu funkciju");
        Client client = findByEmail(username);
        if(client == null) {
            return new ResponseEntity<>("Ne postoji", HttpStatus.NOT_FOUND);
        }
        if(client.isEnabled()) {
            return new ResponseEntity<>("Vec aktiviran", HttpStatus.BAD_REQUEST);
        }

        client.setEnabled(true);
        client.setActive(true);
        save(client);
        return new ResponseEntity<>("super", HttpStatus.OK);

    }
}
