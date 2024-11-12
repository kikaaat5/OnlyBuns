package com.example.OnlyBuns.service;

import com.example.OnlyBuns.model.Address;
import com.example.OnlyBuns.repository.AddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AddressService {
    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    public AddressService(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    public Address findByStreetAndCityAndPostalCodeAndCountry(String street, String city, Integer postalCode, String country) {
        return addressRepository.findByStreetAndCityAndPostalCodeAndCountry(street, city, postalCode, country);
    }

    public Address save(Address address) {
        return addressRepository.save(address);
    }
}
