package com.example.OnlyBuns.controller;

import com.example.OnlyBuns.model.Client;
import com.example.OnlyBuns.model.Role;
import com.example.OnlyBuns.service.*;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.servlet.http.HttpServletResponse;

import com.example.OnlyBuns.dto.JwtAuthenticationRequest;
import com.example.OnlyBuns.dto.UserRequest;
import com.example.OnlyBuns.dto.UserTokenState;
import com.example.OnlyBuns.model.User;
import com.example.OnlyBuns.util.TokenUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.UriComponentsBuilder;

//Kontroler zaduzen za autentifikaciju korisnika
@RestController
@RequestMapping(value = "/auth", produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthenticationController {

	private final Logger LOG = LoggerFactory.getLogger(AuthenticationController.class);

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

	@PostMapping("/login")
	public ResponseEntity<UserTokenState> createAuthenticationToken(
			@RequestBody JwtAuthenticationRequest authenticationRequest, HttpServletResponse response) {

		Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
				authenticationRequest.getEmail(), authenticationRequest.getPassword()));


		SecurityContextHolder.getContext().setAuthentication(authentication);

		User user = (User) authentication.getPrincipal();
		String jwt = tokenUtils.generateToken(user.getUsername());
		int expiresIn = tokenUtils.getExpiredIn();
		Role role = user.getRoles().get(0);
		return ResponseEntity.ok(new UserTokenState(jwt, expiresIn, role.getName()));
	}

	public ResponseEntity<String> loginRateLimiterFallback(JwtAuthenticationRequest authenticationRequest, HttpServletResponse response, Throwable throwable) {
		return ResponseEntity.status(429).body("Too many login attempts. Please try again later.");
	}

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

	public String getClientIp() {
		ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		if (attrs != null) {
			return attrs.getRequest().getRemoteAddr();
		}
		return "0.0.0.0";
	}
}