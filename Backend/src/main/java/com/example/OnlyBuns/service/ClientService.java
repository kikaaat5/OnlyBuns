package com.example.OnlyBuns.service;

import ch.qos.logback.core.net.SyslogOutputStream;
import com.example.OnlyBuns.dto.AddressDto;
import com.example.OnlyBuns.dto.ClientDto;
import com.example.OnlyBuns.dto.UserRequest;
import com.example.OnlyBuns.model.Address;
import com.example.OnlyBuns.model.Role;
import com.example.OnlyBuns.model.User;
import com.example.OnlyBuns.repository.AddressRepository;
import com.example.OnlyBuns.repository.ClientRepository;
import com.example.OnlyBuns.model.Client;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import jakarta.annotation.PostConstruct;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ClientService {
    private final ClientRepository clientRepository;
    public UsernameBloomFilter bloomFilter = new UsernameBloomFilter(List.of()); // init praznim

    @Autowired
    public ClientService(ClientRepository clientRepository) {

        this.clientRepository = clientRepository;
        initializeBloomFilter();
    }

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleService roleService;

    @Autowired
    private AddressService addressService;

    public void initializeBloomFilter() {
        List<String> allUsernames = clientRepository.findAll()
                .stream()
                .map(Client::getUsername)
                .toList();

        this.bloomFilter = new UsernameBloomFilter(allUsernames);
        System.out.println("✅ Bloom Filter spreman sa " + allUsernames.size() + " korisnika.");
    }

    public boolean checkBloomFilter(String username) {
        initializeBloomFilter();
        if (bloomFilter == null) {
            // U slučaju da Bloom filter još nije inicijalizovan
            System.out.println("⚠ Bloom filter nije inicijalizovan, pretpostavljamo da korisnik ne postoji.");
            return true; // Dozvoli registraciju
        }
        return !bloomFilter.probablyExists(username);
    }

    public List<ClientDto> findAll() {
        List<Client> clients = clientRepository.findAll();
        return clients.stream().map(this::mapToDto).toList();
    }

    public ClientDto getById(int id) {
        return clientRepository.findById(id).map(this::mapToDto).orElse(null);
    }
    private ClientDto mapToDto(Client client) {
        ClientDto clientDto = new ClientDto();
        clientDto.setId(client.getId());
        clientDto.setUsername(client.getUsername());
        clientDto.setPassword(client.getPassword()); // Izbegavaj ako nije potrebno
        clientDto.setName(client.getFirstName());
        clientDto.setLastname(client.getLastName());
        clientDto.setEmail(client.getEmail());
        String roles = client.getRoles()
                .stream()
                .map(Role::getName) // Pretpostavlja se da `Role` ima `name` polje
                .reduce((role1, role2) -> role1 + ", " + role2)
                .orElse(null);
        clientDto.setRole(roles); // Pretpostavka da `Role` ima `getName()`
        clientDto.setNumberOfPosts(client.getNumberOfPosts());
        clientDto.setFollowers(client.getFollowers());
        clientDto.setFollowing(client.getFollowing());
        clientDto.setActive(client.isActive());

        // Mapiranje adrese (ako postoji)
        if (client.getAddress() != null) {
            AddressDto addressDto = new AddressDto();
            addressDto.setCity(client.getAddress().getCity());
            addressDto.setStreet(client.getAddress().getStreet());
            addressDto.setCountry(client.getAddress().getCountry());
            addressDto.setPostalCode(client.getAddress().getPostalCode());
            clientDto.setAddress(addressDto);

            // Direktna polja adrese
            clientDto.setCity(client.getAddress().getCity());
            clientDto.setStreet(client.getAddress().getStreet());
            clientDto.setCountry(client.getAddress().getCountry());
            clientDto.setPostalCode(client.getAddress().getPostalCode());
        }

        return clientDto;
    }

    public void save(Client client){clientRepository.save(client);}

    public Client save(UserRequest userRequest) {
        Client c = new Client();
        Address address = addressService.findByStreetAndCityAndPostalCodeAndCountry(
                userRequest.getStreet(),
                userRequest.getCity(),
                userRequest.getPostalCode(),
                userRequest.getCountry());

        if (address == null) {
            address = new Address(userRequest.getStreet(),
                    userRequest.getCity(),
                    userRequest.getPostalCode(),
                    userRequest.getCountry());
            addressService.save(address);
        }

        c.setUsername(userRequest.getUsername());

        // pre nego sto postavimo lozinku u atribut hesiramo je kako bi se u bazi nalazila hesirana lozinka
        // treba voditi racuna da se koristi isi password encoder bean koji je postavljen u AUthenticationManager-u kako bi koristili isti algoritam
        c.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        c.setFirstName(userRequest.getFirstname());
        c.setAddress(address);
        c.setLastName(userRequest.getLastname());
        c.setEmail(userRequest.getEmail());
        c.setFollowing(0);
        c.setNumberOfPosts(0);
        List<Role> roles = roleService.findByName("ROLE_CLIENT");
        c.setRoles(roles);

        bloomFilter.add(c.getUsername());

        return this.clientRepository.save(c);
    }

    public boolean activateUser(String email) {
        Client client = clientRepository.findByEmail(email);
        if (client != null) {
            client.setActive(true);
            client.setEnabled(true);// Postavlja korisnika kao aktivnog
            clientRepository.save(client);
            return true;// Spasavanje u bazi
        }
        return false;
    }

    public Client findByEmail(String email) {
        return clientRepository.findByEmail(email);
    }


    public List<Client> findClientsNotLoggedInSince(LocalDateTime date) {
        return clientRepository.findByLastLoginBefore(date);
    }


    public Client findByUsername(String username) {
        return clientRepository.findByName(username);
    }

    public void deleteById(int id) {
        clientRepository.deleteById(id);
    }

    public List<Client> searchByName(String name) {
        return clientRepository.findByFirstnameContainingIgnoreCase(name);
    }

    public List<Client> searchBySurname(String surname) {
        return clientRepository.findByLastnameContainingIgnoreCase(surname);
    }

    public ResponseEntity<String> activate(String username) {
        System.out.println("usao u aktivacionu funkciju");
        Client client = findByEmail(username);
        if(client == null) {
            return new ResponseEntity<>("Ne postoji", HttpStatus.NOT_FOUND);
        }
        if(client.isEnabled()) {
            return new ResponseEntity<>("Vec aktiviran", HttpStatus.BAD_REQUEST);
        }

        client.setEnabled(true);
        client.setActive(true);
        save(client);
        return new ResponseEntity<>("super", HttpStatus.OK);
    }
  
    /*public List<Client> searchByName(String name) {
        return clientRepository.findByNameContainingIgnoreCase(name);
    }

    public List<Client> searchBySurname(String surname) {
        return clientRepository.findBySurnameContainingIgnoreCase(surname);
    }*/


    public List<Client> searchByEmail(String email) {
        return clientRepository.findByEmailContainingIgnoreCase(email);
    }

    public List<Client> searchByNumberOfPostsInRange(int minPosts, int maxPosts) {
        return clientRepository.findByNumberOfPostsInRange(minPosts, maxPosts);
    }

    public List<Client> sortByFollowingCount() {
        return clientRepository.findAllByOrderByFollowingDesc();
    }

    public List<Client> sortByEmail() {
        return clientRepository.findAllByOrderByEmailAsc();
    }
}
