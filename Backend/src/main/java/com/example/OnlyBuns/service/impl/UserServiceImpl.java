package com.example.OnlyBuns.service.impl;

import java.util.List;

import com.example.OnlyBuns.dto.ChangePasswordDto;
import com.example.OnlyBuns.dto.UserRequest;
import com.example.OnlyBuns.model.Address;
import com.example.OnlyBuns.model.Role;
import com.example.OnlyBuns.model.User;
import com.example.OnlyBuns.repository.UserRepository;
import com.example.OnlyBuns.service.AddressService;
import com.example.OnlyBuns.service.RoleService;
import com.example.OnlyBuns.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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

	@Autowired
	private AddressService addressService;

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

	@Transactional(readOnly = false)
	@Override
	public User save(UserRequest userRequest) {
		/*if (userRepository.findByUsername(userRequest.getUsername()) != null) {
			throw new IllegalArgumentException("Korisničko ime već postoji.");
		}

		if (userRepository.findByEmail(userRequest.getEmail()) != null) {
			throw new IllegalArgumentException("Email već postoji.");
		}
		*/

		Address address = addressService.findByStreetAndCityAndPostalCodeAndCountry(
				userRequest.getStreet(),
				userRequest.getCity(),
				userRequest.getPostalCode(),
				userRequest.getCountry());

		if (address == null) {
			address = new Address(userRequest.getStreet(),
					userRequest.getCity(),
					userRequest.getPostalCode(),
					userRequest.getCountry());
			addressService.save(address);
		}
		User u = new User();
		u.setUsername(userRequest.getUsername());
		u.setPassword(passwordEncoder.encode(userRequest.getPassword()));
		u.setAddress(address);
		u.setFirstName(userRequest.getFirstname());
		u.setLastName(userRequest.getLastname());
		u.setEnabled(true);
		u.setEmail(userRequest.getEmail());
		List<Role> roles = roleService.findByName("ROLE_CLIENT");
		u.setRoles(roles);
		
		return this.userRepository.save(u);
	}

	public User updateUser(Long id, UserRequest userRequest) {
		Address address = addressService.findByStreetAndCityAndPostalCodeAndCountry(
				userRequest.getStreet(),
				userRequest.getCity(),
				userRequest.getPostalCode(),
				userRequest.getCountry());

		if (address == null) {
			address = new Address(userRequest.getStreet(),
					userRequest.getCity(),
					userRequest.getPostalCode(),
					userRequest.getCountry());
			addressService.save(address);
		}

		System.out.println(address.getStreet());
		User u = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found"));
		// pre nego sto postavimo lozinku u atribut hesiramo je kako bi se u bazi nalazila hesirana lozinka
		// treba voditi racuna da se koristi isi password encoder bean koji je postavljen u AUthenticationManager-u kako bi koristili isti algoritam
		//if (userRequest.getPassword() != null && !userRequest.getPassword().isEmpty()) {
			//u.setPassword(passwordEncoder.encode(userRequest.getPassword()));
		//}
		u.setAddress(address);
		u.setFirstName(userRequest.getFirstname());
		u.setLastName(userRequest.getLastname());
		u.setEnabled(true);
		List<Role> roles = roleService.findByName(userRequest.getRole());
		u.setRoles(roles);

		return this.userRepository.save(u);
	}

	public User updateUsersPassword(Long id, ChangePasswordDto changePasswordDto) {
		// Pronađi korisnika po ID-ju ili izbaci grešku
		User user = userRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("User not found"));

		// Provjeri da li se unesena stara lozinka poklapa sa postojećom (hashovanom) lozinkom
		if (!passwordEncoder.matches(changePasswordDto.getOldPassword(), user.getPassword())) {
			throw new IllegalArgumentException("Old password is incorrect");
		}

		// Postavi novu lozinku (hashovana)
		user.setPassword(passwordEncoder.encode(changePasswordDto.getNewPassword()));

		// Sačuvaj korisnika u bazi
		userRepository.save(user);

		return user; // Možeš vratiti korisnika ili samo potvrditi promjenu
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user;
		if (username.matches("^[\\w\\.-]+@[a-zA-Z\\d\\.-]+\\.[a-zA-Z]{2,}$")) {
			user = userRepository.findByEmail(username);
		} else {
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
