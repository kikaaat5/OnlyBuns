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
}
