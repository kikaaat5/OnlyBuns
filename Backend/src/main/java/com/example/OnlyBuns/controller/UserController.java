package com.example.OnlyBuns.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.OnlyBuns.dto.AddressDto;
import com.example.OnlyBuns.dto.UserRequest;
import com.example.OnlyBuns.model.User;
import com.example.OnlyBuns.model.Role;
import com.example.OnlyBuns.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;
import org.modelmapper.ModelMapper;

// Primer kontrolera cijim metodama mogu pristupiti samo autorizovani korisnici
@RestController
@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin
public class UserController {


	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private UserService userService;

	@Autowired
	private ModelMapper modelMapper;

	// Za pristup ovoj metodi neophodno je da ulogovani korisnik ima ADMIN ulogu
	// Ukoliko nema, server ce vratiti gresku 403 Forbidden
	// Korisnik jeste autentifikovan, ali nije autorizovan da pristupi resursu
	@GetMapping("/user/{userId}")
	@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CLIENT')")
	public User loadById(@PathVariable Long userId) {
		return this.userService.findById(userId);
	}

	@GetMapping("/user/all")
	@PreAuthorize("hasRole('ADMIN')")
	public List<User> loadAll() {
		return this.userService.findAll();
	}

	@GetMapping("/whoami")
	@PreAuthorize("hasAnyRole('ADMIN', 'CLIENT')")
	public UserRequest user(Principal user) {
		System.out.println("ususussusuusuassa");
		User u = this.userService.findByEmail(user.getName());
		UserRequest ur = modelMapper.map(u, UserRequest.class);
		if (u.getRoles() != null && !u.getRoles().isEmpty()) {
			String roles = u.getRoles()
					.stream()
					.map(Role::getName) // Pretpostavlja se da `Role` ima `name` polje
					.reduce((role1, role2) -> role1 + ", " + role2)
					.orElse(null);
			ur.setRole(roles);
		} else {
			ur.setRole(null);
		}
		AddressDto adr = modelMapper.map(u.getAddress(), AddressDto.class);
		ur.setAddress(adr);
		return ur;
	}


	@GetMapping("/foo")
    public Map<String, String> getFoo() {
        Map<String, String> fooObj = new HashMap<>();
        fooObj.put("foo", "bar");
        return fooObj;
    }

	@Transactional
	@PostMapping("/user/{id}")
	public ResponseEntity<UserRequest> updateUser(@PathVariable Long id, @RequestBody UserRequest userUpdateDto) {
		User updatedUser = userService.updateUser(id, userUpdateDto);
		UserRequest ur = modelMapper.map(updatedUser, UserRequest.class);
		System.out.println("Ažurirani korisnik: " + updatedUser);
		return ResponseEntity.ok(ur);
	}
}
