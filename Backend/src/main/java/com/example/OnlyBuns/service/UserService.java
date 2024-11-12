package com.example.OnlyBuns.service;

import com.example.OnlyBuns.dto.UserRequest;
import com.example.OnlyBuns.model.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserService {
    User findById(Long id);
    User findByUsername(String username);
    //User findByEmail(String email);
    List<User> findAll ();
	User save(UserRequest userRequest);
}
