package com.example.OnlyBuns.service;

import com.example.OnlyBuns.dto.ClientDto;
//import com.example.OnlyBuns.exception.ResourceNotFoundException;
import com.example.OnlyBuns.exception.ResourceNotFoundException;
import com.example.OnlyBuns.model.Client;
import com.example.OnlyBuns.model.FollowRelation;
import com.example.OnlyBuns.model.User;
import com.example.OnlyBuns.repository.FollowRelationRepository;
import com.example.OnlyBuns.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FollowRelationService {
    private final FollowRelationRepository followRelationRepository;
    private final ClientRepository clientRepository;

    @Autowired
    public FollowRelationService(FollowRelationRepository followRelationRepository, ClientRepository clientRepository) {
        this.followRelationRepository = followRelationRepository;
        this.clientRepository = clientRepository;
    }
    @Transactional
    public String followClient(Integer followerClientId, Integer followedClientId) {
        if (followerClientId.equals(followedClientId)) {
            throw new IllegalArgumentException("Klijent ne može pratiti samog sebe.");
        }

        Client follower = clientRepository.findById(followerClientId)
                .orElseThrow(() -> new ResourceNotFoundException("Klijent sa ID " + followerClientId + " nije pronađen."));
        Client followed = clientRepository.findById(followedClientId)
                .orElseThrow(() -> new ResourceNotFoundException("Klijent sa ID " + followedClientId + " nije pronađen."));

        Optional<FollowRelation> existingFollow = followRelationRepository.findByFollowerAndFollowed(follower, followed);
        if (existingFollow.isPresent()) {
            throw new IllegalStateException("Klijent već prati ovaj nalog.");
        }

        FollowRelation follow = new FollowRelation();
        follow.setFollower(follower); // Sada setujemo Client entitete
        follow.setFollowed(followed); // Sada setujemo Client entitete

        followRelationRepository.save(follow);

        follower.setFollowing(follower.getFollowing() + 1);
        clientRepository.save(follower);

        followed.setFollowers(followed.getFollowers() + 1);
        clientRepository.save(followed);

        return "Uspešno praćenje klijenta " + followed.getUsername();
    }
    @Transactional
    public String unfollowClient(Integer followerClientId, Integer followedClientId) {
        Client follower = clientRepository.findById(followerClientId)
                .orElseThrow(() -> new ResourceNotFoundException("Klijent sa ID " + followerClientId + " nije pronađen."));
        Client followed = clientRepository.findById(followedClientId)
                .orElseThrow(() -> new ResourceNotFoundException("Klijent sa ID " + followedClientId + " nije pronađen."));

        FollowRelation follow = followRelationRepository.findByFollowerAndFollowed(follower, followed)
                .orElseThrow(() -> new ResourceNotFoundException("Praćenje ne postoji."));

        followRelationRepository.delete(follow);

        if (follower.getFollowing() > 0) {
            follower.setFollowing(follower.getFollowing() - 1);
        }
        clientRepository.save(follower);

        if (followed.getFollowers() > 0) {
            followed.setFollowers(followed.getFollowers() - 1);
        }
        clientRepository.save(followed);

        return "Uspešno prekinuto praćenje klijenta " + followed.getUsername();
    }
    public boolean isFollowing(Integer followerClientId, Integer followedClientId) {
        Client follower = clientRepository.findById(followerClientId)
                .orElseThrow(() -> new ResourceNotFoundException("Klijent sa ID " + followerClientId + " nije pronađen."));
        Client followed = clientRepository.findById(followedClientId)
                .orElseThrow(() -> new ResourceNotFoundException("Klijent sa ID " + followedClientId + " nije pronađen."));
        return followRelationRepository.findByFollowerAndFollowed(follower, followed).isPresent();
    }
    public List<ClientDto> getFollowing(Integer clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Klijent sa ID " + clientId + " nije pronađen."));

        return followRelationRepository.findByFollower(client).stream()
                .map(FollowRelation::getFollowed) // Dohvatamo entitete klijenata koje prati
                .map(this::convertToClientDto) // Konvertujemo svaki Client entitet u ClientDto
                .collect(Collectors.toList());
    }
    public List<ClientDto> getFollowers(Integer clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Klijent sa ID " + clientId + " nije pronađen."));

        return followRelationRepository.findByFollowed(client).stream()
                .map(FollowRelation::getFollower) // Dohvatamo entitete klijenata koji prate
                .map(this::convertToClientDto) // Konvertujemo svaki Client entitet u ClientDto
                .collect(Collectors.toList());
    }
    public long getFollowingCount(Integer clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Klijent sa ID " + clientId + " nije pronađen."));
        return followRelationRepository.countByFollower(client);
    }
    public long getFollowersCount(Integer clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Klijent sa ID " + clientId + " nije pronađen."));
        return followRelationRepository.countByFollowed(client);
    }
    private ClientDto convertToClientDto(Client client) {
        ClientDto clientDto = new ClientDto();
        clientDto.setId(client.getId());
        clientDto.setUsername(client.getUsername());
        clientDto.setEmail(client.getEmail());
        clientDto.setFirstname(client.getFirstName());
        clientDto.setLastname(client.getLastName());

        return clientDto;
    }
}