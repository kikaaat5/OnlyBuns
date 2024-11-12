package com.example.OnlyBuns.service.impl;

import java.util.List;

import com.example.OnlyBuns.dto.UserRequest;
import com.example.OnlyBuns.model.Role;
import com.example.OnlyBuns.model.User;
import com.example.OnlyBuns.repository.UserRepository;
import com.example.OnlyBuns.service.RoleService;
import com.example.OnlyBuns.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService, UserDetailsService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private RoleService roleService;

	@Override
	public User findByUsername(String username) throws UsernameNotFoundException {
		return userRepository.findByEmail(username);
	}

	public User findById(Long id) throws AccessDeniedException {
		return userRepository.findById(id).orElseGet(null);
	}

	public User findByEmail(String email) throws AccessDeniedException {
		System.out.println(email);
		return userRepository.findByUsername(email);
	}

	public List<User> findAll() throws AccessDeniedException {
		return userRepository.findAll();
	}

	@Override
	public User save(UserRequest userRequest) {
		User u = new User();
		u.setUsername(userRequest.getUsername());
		// pre nego sto postavimo lozinku u atribut hesiramo je kako bi se u bazi nalazila hesirana lozinka
		// treba voditi racuna da se koristi isi password encoder bean koji je postavljen u AUthenticationManager-u kako bi koristili isti algoritam
		u.setPassword(passwordEncoder.encode(userRequest.getPassword()));
		
		u.setFirstName(userRequest.getFirstname());
		u.setLastName(userRequest.getLastname());
		u.setEnabled(true);
		u.setEmail(userRequest.getEmail());

		// u primeru se registruju samo obicni korisnici i u skladu sa tim im se i dodeljuje samo rola USER
		List<Role> roles = roleService.findByName("ROLE_CLIENT");
		u.setRoles(roles);
		
		return this.userRepository.save(u);
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user;
		if(username.contains("@")){
			 user = userRepository.findByEmail(username);
		}
		else{
			 user = userRepository.findByUsername(username);
		}
		if (user == null) {
			throw new UsernameNotFoundException(String.format("No user found with username '%s'.", username));
		} else {
			return user;
		}
	}

	public UserDetails loadUserByUsernameNew(String username) throws UsernameNotFoundException {
		User user = userRepository.findByEmail(username);
		if (user == null) {
			throw new UsernameNotFoundException(String.format("No user found with username '%s'.", username));
		} else {
			return user;
		}
	}
}
