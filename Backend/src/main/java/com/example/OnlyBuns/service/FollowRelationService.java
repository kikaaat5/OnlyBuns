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
    /*@Transactional
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
    }*/
    @Transactional
    public String followClient(Integer followerClientId, Integer followedClientId) {
        if (followerClientId.equals(followedClientId)) {
            throw new IllegalArgumentException("Klijent ne može pratiti samog sebe.");
        }


        Client actualFollower;
        Client actualFollowed;

        // Odredi redosled zaključavanja na osnovu ID-jeva da sprečiš deadlock
        if (followerClientId < followedClientId) {
            actualFollower = clientRepository.findByIdForUpdate(followerClientId)
                    .orElseThrow(() -> new ResourceNotFoundException("Pratilac sa ID " + followerClientId + " nije pronađen."));
            actualFollowed = clientRepository.findByIdForUpdate(followedClientId)
                    .orElseThrow(() -> new ResourceNotFoundException("Praćeni klijent sa ID " + followedClientId + " nije pronađen."));
        } else {
            // Obrnut redosled dohvaćanja da se spreči deadlock
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
        follow.setFollower(actualFollower); // Koristi zaključane entitete
        follow.setFollowed(actualFollowed); // Koristi zaključane entitete

        followRelationRepository.save(follow);

        // Inkrementiranje brojača (sada sigurno unutar zaključane transakcije)
        actualFollower.setFollowing(actualFollower.getFollowing() + 1);
        actualFollowed.setFollowers(actualFollowed.getFollowers() + 1);

        // Ažurirani entiteti će biti sačuvani automatski na kraju transakcije zbog Transactional anotacije
        // i jer su entiteti u "managed" stanju. Međutim, eksplicitno save() je dobra praksa za jasnoću.
        clientRepository.save(actualFollower);
        clientRepository.save(actualFollowed);

        return "Uspešno praćenje klijenta " + actualFollowed.getUsername();
    }

    @Transactional
    public String unfollowClient(Integer followerClientId, Integer followedClientId) {
        // NOVO: Dohvatanje korisnika sa zaključavanjem (isti redosled kao kod follow-a)
        Client actualFollower;
        Client actualFollowed;

        // Odredi redosled zaključavanja na osnovu ID-jeva da sprečiš deadlock
        if (followerClientId < followedClientId) {
            actualFollower = clientRepository.findByIdForUpdate(followerClientId)
                    .orElseThrow(() -> new ResourceNotFoundException("Pratilac sa ID " + followerClientId + " nije pronađen."));
            actualFollowed = clientRepository.findByIdForUpdate(followedClientId)
                    .orElseThrow(() -> new ResourceNotFoundException("Praćeni klijent sa ID " + followedClientId + " nije pronađen."));
        } else {
            // Obrnut redosled dohvaćanja da se spreči deadlock
            actualFollowed = clientRepository.findByIdForUpdate(followedClientId)
                    .orElseThrow(() -> new ResourceNotFoundException("Praćeni klijent sa ID " + followedClientId + " nije pronađen."));
            actualFollower = clientRepository.findByIdForUpdate(followerClientId)
                    .orElseThrow(() -> new ResourceNotFoundException("Pratilac sa ID " + followerClientId + " nije pronađen."));
        }


        FollowRelation follow = followRelationRepository.findByFollowerAndFollowed(actualFollower, actualFollowed)
                .orElseThrow(() -> new ResourceNotFoundException("Praćenje ne postoji."));

        followRelationRepository.delete(follow);

        // Dekrementiranje brojača
        if (actualFollower.getFollowing() > 0) {
            actualFollower.setFollowing(actualFollower.getFollowing() - 1);
        }
        if (actualFollowed.getFollowers() > 0) {
            actualFollowed.setFollowers(actualFollowed.getFollowers() - 1);
        }

        // Eksplicitno save radi jasnoće, mada @Transactional bi trebalo da se pobrine za to
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