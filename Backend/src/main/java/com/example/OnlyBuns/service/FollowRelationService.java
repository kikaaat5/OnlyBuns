package com.example.OnlyBuns.service;

import com.example.OnlyBuns.dto.ClientDto;
import com.example.OnlyBuns.exception.ResourceNotFoundException;
import com.example.OnlyBuns.model.Client;
import com.example.OnlyBuns.model.FollowRelation;
import jakarta.transaction.Transactional;
import com.example.OnlyBuns.repository.FollowRelationRepository;
import com.example.OnlyBuns.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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


        Client actualFollower;
        Client actualFollowed;

        if (followerClientId < followedClientId) {
            actualFollower = clientRepository.findByIdForUpdate(followerClientId)
                    .orElseThrow(() -> new ResourceNotFoundException("Pratilac sa ID " + followerClientId + " nije pronađen."));
            actualFollowed = clientRepository.findByIdForUpdate(followedClientId)
                    .orElseThrow(() -> new ResourceNotFoundException("Praćeni klijent sa ID " + followedClientId + " nije pronađen."));
        } else {
            actualFollowed = clientRepository.findByIdForUpdate(followedClientId)
                    .orElseThrow(() -> new ResourceNotFoundException("Praćeni klijent sa ID " + followedClientId + " nije pronađen."));
            actualFollower = clientRepository.findByIdForUpdate(followerClientId)
                    .orElseThrow(() -> new ResourceNotFoundException("Pratilac sa ID " + followerClientId + " nije pronađen."));
        }

        Optional<FollowRelation> existingFollow = followRelationRepository.findByFollowerAndFollowed(actualFollower, actualFollowed);
        if (existingFollow.isPresent()) {
            throw new IllegalStateException("Klijent već prati ovaj nalog.");
        }

        FollowRelation follow = new FollowRelation();
        follow.setFollower(actualFollower);
        follow.setFollowed(actualFollowed);

        followRelationRepository.save(follow);

        actualFollower.setFollowing(actualFollower.getFollowing() + 1);
        actualFollowed.setFollowers(actualFollowed.getFollowers() + 1);

        clientRepository.save(actualFollower);
        clientRepository.save(actualFollowed);

        return "Uspešno praćenje klijenta " + actualFollowed.getUsername();
    }

    @Transactional
    public String unfollowClient(Integer followerClientId, Integer followedClientId) {
        Client actualFollower;
        Client actualFollowed;

        if (followerClientId < followedClientId) {
            actualFollower = clientRepository.findByIdForUpdate(followerClientId)
                    .orElseThrow(() -> new ResourceNotFoundException("Pratilac sa ID " + followerClientId + " nije pronađen."));
            actualFollowed = clientRepository.findByIdForUpdate(followedClientId)
                    .orElseThrow(() -> new ResourceNotFoundException("Praćeni klijent sa ID " + followedClientId + " nije pronađen."));
        } else {
            actualFollowed = clientRepository.findByIdForUpdate(followedClientId)
                    .orElseThrow(() -> new ResourceNotFoundException("Praćeni klijent sa ID " + followedClientId + " nije pronađen."));
            actualFollower = clientRepository.findByIdForUpdate(followerClientId)
                    .orElseThrow(() -> new ResourceNotFoundException("Pratilac sa ID " + followerClientId + " nije pronađen."));
        }


        FollowRelation follow = followRelationRepository.findByFollowerAndFollowed(actualFollower, actualFollowed)
                .orElseThrow(() -> new ResourceNotFoundException("Praćenje ne postoji."));

        followRelationRepository.delete(follow);

        if (actualFollower.getFollowing() > 0) {
            actualFollower.setFollowing(actualFollower.getFollowing() - 1);
        }
        if (actualFollowed.getFollowers() > 0) {
            actualFollowed.setFollowers(actualFollowed.getFollowers() - 1);
        }

        clientRepository.save(actualFollower);
        clientRepository.save(actualFollowed);

        return "Uspešno prekinuto praćenje klijenta " + actualFollowed.getUsername();
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
                .map(FollowRelation::getFollowed)
                .map(this::convertToClientDto)
                .collect(Collectors.toList());
    }
    public List<ClientDto> getFollowers(Integer clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Klijent sa ID " + clientId + " nije pronađen."));

        return followRelationRepository.findByFollowed(client).stream()
                .map(FollowRelation::getFollower)
                .map(this::convertToClientDto)
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