package com.example.OnlyBuns.controller;

import com.example.OnlyBuns.model.Administrator;
import com.example.OnlyBuns.service.AdministratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/administrators")
public class AdministratorController {

    private final AdministratorService administratorService;

    @Autowired
    public AdministratorController(AdministratorService administratorService) {
        this.administratorService = administratorService;
    }

    @GetMapping
    public List<Administrator> getAllAdministrators() {
        return administratorService.findAll();
    }

    @PostMapping
    public Administrator createAdministrator(@RequestBody Administrator administrator) {
        return administratorService.save(administrator);
    }

    @DeleteMapping("/{id}")
    public void deleteAdministrator(@PathVariable int id) {
        administratorService.deleteById(id);
    }
}
