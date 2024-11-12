package com.example.OnlyBuns.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name="administrator")
public class Administrator extends User {

    public Administrator(Integer id, String email, String username, String password, String name, String surname) {
        super();
    }

    public Administrator() {

    }
}
