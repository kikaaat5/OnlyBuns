package com.example.OnlyBuns.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ClientDto {
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
    private Integer numberOfPosts;
    private Integer following;
    private boolean active;
    private Integer followers;

    public ClientDto() {}

    public ClientDto(Long id, String username, String password, String lastname, String name, String email, AddressDto address, String role, Integer following, Integer numberOfPosts, boolean active, Integer followers) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.lastname = lastname;
        this.name = name;
        this.email = email;
        this.address = address;
        this.role = role;
        this.following = following;
        this.numberOfPosts = numberOfPosts;
        this.active = active;
        this.followers = followers;
    }

    public String getFirstname() {
        return name;
    }

    public void setFirstname(String firstname) {
        this.name = firstname;
    }

}
