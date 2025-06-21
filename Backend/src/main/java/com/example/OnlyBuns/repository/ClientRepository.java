package com.example.OnlyBuns.repository;

import com.example.OnlyBuns.model.Client;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Integer> {

    List<Client> findByFirstnameContainingIgnoreCase(String name);
    List<Client> findByLastnameContainingIgnoreCase(String surname);

  
    @Query("SELECT b from Client b where b.email=:string")
    Client findByEmail(String string);

    @Query("SELECT b from Client b where b.username=:string")
    Client findByName(String string);
    List<Client> findByEmailContainingIgnoreCase(String email);

    @Query("SELECT c FROM Client c WHERE c.numberOfPosts BETWEEN :minPosts AND :maxPosts")
    List<Client> findByNumberOfPostsInRange(@Param("minPosts") int minPosts, @Param("maxPosts") int maxPosts);
    List<Client> findAllByOrderByFollowingDesc();
    List<Client> findAllByOrderByEmailAsc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Client c where c.id = :id")
    Optional<Client> findByIdForUpdate(@Param("id") Integer id);

    @Query("SELECT c FROM Client c WHERE c.enabled = false")
    List<Client> findUnactivatedClients();
}
