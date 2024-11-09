package com.example.OnlyBuns.model;

import com.example.OnlyBuns.enums.UserRole;
import jakarta.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class User {

    @Id
    @SequenceGenerator(name = "mySeqGenV1", sequenceName = "mySeqV1", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mySeqGenV1")
    private Integer id;

    @Column(name="email", unique=false, nullable=false)
    private String email;
    @Column(name="username", unique=false, nullable=false)
    private String username;
    @Column(name="password", unique=false, nullable=false)
    private String password;
    @Column(name="name", unique=false, nullable=false)
    private String name;
    @Column(name="surname", unique=false, nullable=false)
    private String surname;
    @Column(name="role", unique=false, nullable=false)
    private UserRole role;

    public User() {
        super();
    }

    public User(int id, String email, String username, String password, String name, String surname, UserRole role) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.password = password;
        this.name = name;
        this.surname = surname;
        this.role = role;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }
}
