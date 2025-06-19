package com.example.OnlyBuns.repository;

import com.example.OnlyBuns.model.Client;
import com.example.OnlyBuns.model.FollowRelation;
import com.example.OnlyBuns.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowRelationRepository extends JpaRepository<FollowRelation, Integer> {
    // Metoda za proveru da li follower prati followed korisnika
    Optional<FollowRelation> findByFollowerAndFollowed(Client follower, Client followed);

    // Metoda za pronalaženje svih praćenja gde je dati korisnik follower
    List<FollowRelation> findByFollower(Client follower);

    // Metoda za pronalaženje svih praćenja gde je dati korisnik followed
    List<FollowRelation> findByFollowed(Client followed);

    // Metoda za brojanje pratilaca za datog korisnika
    long countByFollowed(Client followed);

    // Metoda za brojanje korisnika koje prati dati korisnik
    long countByFollower(Client follower);
}
