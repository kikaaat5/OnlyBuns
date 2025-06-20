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

    Optional<FollowRelation> findByFollowerAndFollowed(Client follower, Client followed);

    List<FollowRelation> findByFollower(Client follower);

    List<FollowRelation> findByFollowed(Client followed);

    long countByFollowed(Client followed);

    long countByFollower(Client follower);
}
