package com.example.OnlyBuns.controller;

import com.example.OnlyBuns.model.Client;
import org.springframework.web.bind.annotation.CrossOrigin;
import com.example.OnlyBuns.repository.ClientRepository;
import com.example.OnlyBuns.service.ClientService;
import com.example.OnlyBuns.service.UserService;
import com.example.OnlyBuns.service.impl.UserServiceImpl;
import com.example.OnlyBuns.util.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class ActivationController {

    @Autowired
    private TokenUtils tokenUtils;

    @Autowired
    private ClientService clientService;

    @Autowired
    private UserServiceImpl userService;

    @GetMapping("/activate/{token}")
    public ResponseEntity<?> activateAccount(@PathVariable String token) {
        return clientService.activate(tokenUtils.getUsernameFromToken(token));
    }
}

