package com.example.OnlyBuns.service;

import com.example.OnlyBuns.repository.AdministratorRepository;
import com.example.OnlyBuns.model.Administrator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdministratorService {
    private final AdministratorRepository administratorRepository;

    @Autowired
    public AdministratorService(AdministratorRepository administratorRepository) {
        this.administratorRepository = administratorRepository;
    }

    public List<Administrator> findAll() {
        return administratorRepository.findAll();
    }

    public Administrator save(Administrator administrator) {
        return administratorRepository.save(administrator);
    }

    public void deleteById(int id) {
        administratorRepository.deleteById(id);
    }
}
