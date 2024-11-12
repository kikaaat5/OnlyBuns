package com.example.OnlyBuns.repository;

import com.example.OnlyBuns.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressRepository extends JpaRepository<Address, Integer> {
    Address findByStreetAndCityAndPostalCodeAndCountry(String street, String city, Integer postalCode, String country);
}
