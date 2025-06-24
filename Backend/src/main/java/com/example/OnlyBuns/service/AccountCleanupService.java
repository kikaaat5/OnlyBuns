package com.example.OnlyBuns.service;

import com.example.OnlyBuns.model.Client;
import com.example.OnlyBuns.repository.ClientRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AccountCleanupService {
    private static final Logger log = LoggerFactory.getLogger(AccountCleanupService.class);
    private final ClientRepository clientRepository;

    @Autowired
    public AccountCleanupService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    /**
     * Zakazani zadatak koji se izvršava u 02:00 ujutru posljednjeg dana svakog mjeseca.
     * Zadatak briše neaktivirane naloge.
     */
    @Scheduled(cron = "0 0 2 L * ?")
    @Transactional
    public void purgeUnactivatedAccounts() {
        log.info("--- Pokretanje zakazanog zadatka: Brisanje neaktiviranih naloga ---");
        List<Client> clientsToDelete = clientRepository.findUnactivatedClients();

        if (!clientsToDelete.isEmpty()) {
            clientRepository.deleteAll(clientsToDelete);
            System.out.println("Obrisano " + clientsToDelete.size() + " neaktiviranih klijenata.");
        }
    }
}
