package com.example.OnlyBuns.repository;

import com.example.OnlyBuns.model.Client;
import com.example.OnlyBuns.model.FollowRelation;
import com.example.OnlyBuns.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FollowRelationRepository extends JpaRepository<FollowRelation, Integer> {

    Optional<FollowRelation> findByFollowerAndFollowed(Client follower, Client followed);

    List<FollowRelation> findByFollower(Client follower);

    List<FollowRelation> findByFollowed(Client followed);

    long countByFollowed(Client followed);

    long countByFollower(Client follower);

    int countByFollowedAndFollowDateAfter(Client followed, LocalDateTime date);

    @Query("SELECT f.followed.id FROM FollowRelation f WHERE f.follower = :client")
    List<Long> findFollowedClientIds(@Param("client") Client client);
}
