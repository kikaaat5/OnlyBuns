package com.example.OnlyBuns.repository;

import com.example.OnlyBuns.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClientRepository extends JpaRepository<Client, Integer> {
    List<Client> findByFirstnameContainingIgnoreCase(String name);
    List<Client> findByLastnameContainingIgnoreCase(String surname);
    List<Client> findByEmailContainingIgnoreCase(String email);

    @Query("SELECT c FROM Client c WHERE c.numberOfPosts BETWEEN :minPosts AND :maxPosts")
    List<Client> findByNumberOfPostsInRange(@Param("minPosts") int minPosts, @Param("maxPosts") int maxPosts);
    List<Client> findAllByOrderByFollowingDesc();
    List<Client> findAllByOrderByEmailAsc();
}
