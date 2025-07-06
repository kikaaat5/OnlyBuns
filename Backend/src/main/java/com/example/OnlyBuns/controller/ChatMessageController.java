package com.example.OnlyBuns.controller;

import com.example.OnlyBuns.dto.ChatMessageDto;
import com.example.OnlyBuns.dto.MessageSendRequestDto;
import com.example.OnlyBuns.model.Client;
import com.example.OnlyBuns.service.ChatMessageService;
import com.example.OnlyBuns.service.ClientService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping; // <-- KLJUČNI IMPORT ZA WEBSOCKET
import org.springframework.messaging.handler.annotation.Payload;     // <-- KLJUČNI IMPORT ZA WEBSOCKET
import org.springframework.messaging.simp.SimpMessageHeaderAccessor; // <-- KLJUČNI IMPORT ZA DOHVAT PRINCIPALA U WEBSOCKETU
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller; // <-- PROMENJENO IZ @RestController U @Controller
import org.springframework.web.bind.annotation.*; // Ostavljeno zbog @GetMapping i @RequestMapping
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Principal;
import java.util.List;

@Controller // <-- PROMENJENO IZ @RestController U @Controller
@RequestMapping("/api/chat") // Ova anotacija se i dalje koristi za REST endpointe
public class ChatMessageController {

    private static final Logger logger = LoggerFactory.getLogger(ChatMessageController.class);

    private final ChatMessageService chatMessageService;
    private final ClientService clientService;

    public ChatMessageController(ChatMessageService chatMessageService, ClientService clientService) {
        this.chatMessageService = chatMessageService;
        this.clientService = clientService;
    }

    // Pomoćna metoda za dohvat ID-a trenutno ulogovanog klijenta
    private Integer getCurrentClientId() {
        logger.debug("getCurrentClientId: Attempting to get current client ID.");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            logger.warn("getCurrentClientId: User not authenticated or is anonymous. Authentication: {}", authentication);
            throw new IllegalStateException("Korisnik nije autentifikovan. Prijavite se da biste izvršili ovu operaciju.");
        }

        String username = authentication.getName();
        logger.debug("getCurrentClientId: Authenticated username from Principal: {}", username);

        Client client = clientService.findByUsername(username);

        if (client != null) {
            logger.debug("getCurrentClientId: Found client ID: {} for username: {}", client.getId(), username);
            return Math.toIntExact(client.getId());
        } else {
            logger.error("getCurrentClientId: Logged in client with username '{}' not found in database. This indicates a data inconsistency.", username);
            throw new IllegalStateException("Ulogovani klijent sa username-om '" + username + "' nije pronađen u bazi podataka.");
        }
    }

    // *** ISPRAVLJENA METODA ZA SLANJE PORUKA PREKO WEBSOCKET-a ***
    // Koristi @MessageMapping za WebSocket poruke.
    // Putanja "/chat.sendMessage" se kombinuje sa "/app/" prefiksom iz WebSocketConfig-a.
    // Ne vraća ResponseEntity, već void.
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload MessageSendRequestDto request, Principal principal) {
        logger.info("ChatMessageController (WebSocket): Primljen zahtev za slanje poruke za sobu: {}", request.getChatRoomId());

        if (principal == null || principal.getName() == null) {
            logger.error("Korisnik nije autentifikovan za slanje poruke preko WebSocket-a.");
            // Ne možeš vratiti HTTP status, samo prekini izvršenje
            return;
        }

        try {
            // Dohvati username direktno iz Principal objekta
            String username = principal.getName();
            logger.info("Poruku šalje korisnik: {}", username);

            // Pronađi ID korisnika na osnovu username-a
            Client client = clientService.findByUsername(username);
            if (client == null) {
                logger.error("Korisnik '{}' iz tokena nije pronađen u bazi.", username);
                return;
            }
            Integer senderId = Math.toIntExact(client.getId());

            // Pozovi servis sa dobijenim ID-em
            chatMessageService.sendMessage(request, senderId);
            logger.info("Poruka uspešno obrađena od strane servisa.");

        } catch (Exception e) {
            logger.error("Neočekivana greška pri slanju poruke: {}", e.getMessage(), e);
        }
    }

    // REST endpoint za dohvat istorije poruka (ovaj je već radio)
    // Ostaje @GetMapping i vraća ResponseEntity
    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<List<ChatMessageDto>> getChatHistory(
            @PathVariable Integer roomId,
            Principal principal) {
        logger.info("ChatMessageController (REST): Zahtev za istoriju chata za roomId: {}", roomId);
        try {
            Integer userId = getCurrentClientId();
            logger.info("ChatMessageController (REST): Dohvatam istoriju chata za sobu {} za korisnika ID: {}", roomId, userId);
            List<ChatMessageDto> messages = chatMessageService.getChatHistory(roomId, userId);
            logger.info("ChatMessageController (REST): Uspešno dohvaćeno {} poruka za sobu {}", messages.size(), roomId);
            return new ResponseEntity<>(messages, HttpStatus.OK);
        } catch (IllegalStateException e) {
            logger.error("ChatMessageController (REST): Greška autentifikacije/autorizacije (IllegalStateException) pri dohvatu istorije chata: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } catch (EntityNotFoundException e) {
            logger.error("ChatMessageController (REST): Entitet nije pronađen (EntityNotFoundException) pri dohvatu istorije chata: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (SecurityException e) {
            logger.warn("ChatMessageController (REST): Bezbednosna povreda (SecurityException) pri dohvatu istorije chata za sobu {}: {}", roomId, e.getMessage());
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            logger.error("ChatMessageController (REST): Neočekivana greška pri dohvatu istorije chata: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}