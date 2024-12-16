package com.example.OnlyBuns.controller;

import com.example.OnlyBuns.model.Client;
import com.example.OnlyBuns.model.Role;
import com.example.OnlyBuns.service.AdministratorService;
import com.example.OnlyBuns.service.ClientService;
import com.example.OnlyBuns.service.EmailService;
import jakarta.servlet.http.HttpServletResponse;

import com.example.OnlyBuns.dto.JwtAuthenticationRequest;
import com.example.OnlyBuns.dto.UserRequest;
import com.example.OnlyBuns.dto.UserTokenState;
import com.example.OnlyBuns.exception.ResourceConflictException;
import com.example.OnlyBuns.model.User;
import com.example.OnlyBuns.service.UserService;
import com.example.OnlyBuns.util.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Clock;

//Kontroler zaduzen za autentifikaciju korisnika
@RestController
@RequestMapping(value = "/auth", produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthenticationController {

	@Autowired
	private TokenUtils tokenUtils;

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private UserService userService;

	@Autowired
	private ClientService clientService;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private AdministratorService administratorService;
    @Autowired
    private EmailService emailService;

	// Prvi endpoint koji pogadja korisnik kada se loguje.
	// Tada zna samo svoje korisnicko ime i lozinku i to prosledjuje na backend.
	@PostMapping("/login")
	public ResponseEntity<UserTokenState> createAuthenticationToken(
			@RequestBody JwtAuthenticationRequest authenticationRequest, HttpServletResponse response) {
		// Ukoliko kredencijali nisu ispravni, logovanje nece biti uspesno, desice se
		// AuthenticationException
		System.out.println("asdaadaa" + authenticationRequest.getEmail());
		System.out.println("asdaadaa" + authenticationRequest.getPassword());

		Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
				authenticationRequest.getEmail(), authenticationRequest.getPassword()));

		// Ukoliko je autentifikacija uspesna, ubaci korisnika u trenutni security
		// kontekst
		SecurityContextHolder.getContext().setAuthentication(authentication);

		// Kreiraj token za tog korisnika
		User user = (User) authentication.getPrincipal();
		String jwt = tokenUtils.generateToken(user.getUsername());
		int expiresIn = tokenUtils.getExpiredIn();
		Role role = user.getRoles().get(0);
		// Vrati token kao odgovor na uspesnu autentifikaciju
		return ResponseEntity.ok(new UserTokenState(jwt, expiresIn, role.getName()));
	}

	// Endpoint za registraciju novog korisnika
	@PostMapping("/signup")
	public ResponseEntity<User> addUser(@RequestBody UserRequest userRequest, UriComponentsBuilder ucBuilder) {

		if (userRequest.getFirstname() == null || userRequest.getFirstname().trim().isEmpty() ||
				userRequest.getLastname() == null || userRequest.getLastname().trim().isEmpty() ||
				userRequest.getCity() == null || userRequest.getCity().trim().isEmpty() ||
				userRequest.getCountry() == null || userRequest.getCountry().trim().isEmpty() ||
				userRequest.getStreet() == null || userRequest.getStreet().trim().isEmpty() ||
				userRequest.getPostalCode() == null  ||
				userRequest.getEmail() == null || userRequest.getEmail().trim().isEmpty() ||
				userRequest.getUsername() == null || userRequest.getUsername().trim().isEmpty() ||
				userRequest.getPassword() == null || userRequest.getPassword().trim().isEmpty()) {

			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		User existUser = this.userService.findByUsername(userRequest.getEmail());
		User usUser = this.userService.findByEmail(userRequest.getUsername());

		String pass = passwordEncoder.encode(userRequest.getPassword());

		if (existUser != null) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		if (usUser != null) {
			return new ResponseEntity<>(HttpStatus.CONFLICT);
		}

		try{
			if(userRequest.getRole().equals("ROLE_CLIENT")){
				System.out.println("usaooo");
				Client client = this.clientService.save(userRequest);
				emailService.sendRegistrationActivation(client);
				return new ResponseEntity<>(client, HttpStatus.CREATED);
			}
			else{
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}



	}
}