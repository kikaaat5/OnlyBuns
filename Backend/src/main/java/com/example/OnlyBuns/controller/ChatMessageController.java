package com.example.OnlyBuns.controller;

import com.example.OnlyBuns.dto.ChatMessageDto;
import com.example.OnlyBuns.dto.MessageSendRequestDto;
import com.example.OnlyBuns.model.Client;
import com.example.OnlyBuns.service.ChatMessageService;
import com.example.OnlyBuns.service.ClientService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/api/chat")
public class ChatMessageController {

    private static final Logger logger = LoggerFactory.getLogger(ChatMessageController.class);

    private final ChatMessageService chatMessageService;
    private final ClientService clientService;

    public ChatMessageController(ChatMessageService chatMessageService, ClientService clientService) {
        this.chatMessageService = chatMessageService;
        this.clientService = clientService;
    }

    private Integer getCurrentClientId() {
        logger.debug("getCurrentClientId: Attempting to get current client ID.");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new IllegalStateException("Korisnik nije autentifikovan. Prijavite se da biste izvršili ovu operaciju.");
        }

        String username = authentication.getName();
        Client client = clientService.findByUsername(username);

        if (client != null) {
            return Math.toIntExact(client.getId());
        } else {
            throw new IllegalStateException("Ulogovani klijent sa username-om '" + username + "' nije pronađen u bazi podataka.");
        }
    }

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload MessageSendRequestDto request, Principal principal) {
        if (principal == null || principal.getName() == null) {
            return;
        }

        try {
            String username = principal.getName();
            Client client = clientService.findByUsername(username);
            if (client == null) {
                return;
            }
            Integer senderId = Math.toIntExact(client.getId());

            chatMessageService.sendMessage(request, senderId);

        } catch (Exception e) {
            logger.error("Neočekivana greška pri slanju poruke: {}", e.getMessage(), e);
        }
    }

    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<List<ChatMessageDto>> getChatHistory(
            @PathVariable Integer roomId,
            Principal principal,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            Integer userId = getCurrentClientId();
            List<ChatMessageDto> messages = chatMessageService.getChatHistory(roomId, userId, limit);
            return new ResponseEntity<>(messages, HttpStatus.OK);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (SecurityException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}