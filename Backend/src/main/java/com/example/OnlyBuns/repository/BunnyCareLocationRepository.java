package com.example.OnlyBuns.repository;

import com.example.OnlyBuns.model.BunnyCareLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BunnyCareLocationRepository extends JpaRepository<BunnyCareLocation, Long> {
}
