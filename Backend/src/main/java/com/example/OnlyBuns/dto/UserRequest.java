package com.example.OnlyBuns.dto;

// DTO koji preuzima podatke iz HTML forme za registraciju
public class UserRequest {

	private Long id;

	private String username;

	private String password;

	private String name;

	private String lastname;
	
	private String email;

	private AddressDto address;

	private String role;

	private String city;

	private String street;

	private String country;

	private Integer postalCode;

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public Integer getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(Integer postalCode) {
		this.postalCode = postalCode;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getFirstname() {
		return name;
	}

	public void setFirstname(String firstname) {
		this.name = firstname;
	}

	public AddressDto getAddress() { return address; }

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getRole() { return role; }

	public void setRole(String role) { this.role = role; }

	public void setAddress(AddressDto address) {
		this.address = address;
	}
}
