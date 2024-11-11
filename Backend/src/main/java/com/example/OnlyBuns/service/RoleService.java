package com.example.OnlyBuns.service;

import com.example.OnlyBuns.model.Role;

import java.util.List;

public interface RoleService {
	Role findById(Long id);
	List<Role> findByName(String name);
}
