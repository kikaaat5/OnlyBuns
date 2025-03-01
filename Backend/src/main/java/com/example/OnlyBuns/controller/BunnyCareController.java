package com.example.OnlyBuns.controller;

import com.example.OnlyBuns.model.BunnyCareLocation;
import com.example.OnlyBuns.repository.BunnyCareLocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/bunny-care")
@RequiredArgsConstructor
public class BunnyCareController {

    private final BunnyCareLocationRepository repository;

    @GetMapping("/locations")
    public List<BunnyCareLocation> getAllLocations() {
        return repository.findAll();
    }
}
