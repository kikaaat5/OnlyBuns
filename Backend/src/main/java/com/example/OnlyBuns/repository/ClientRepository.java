package com.example.OnlyBuns.repository;

import com.example.OnlyBuns.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ClientRepository extends JpaRepository<Client, Integer> {
    @Query("SELECT b from Client b where b.email=:string")
    Client findByEmail(String string);
}
