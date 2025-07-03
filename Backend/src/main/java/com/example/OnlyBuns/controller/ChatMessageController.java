package com.example.OnlyBuns.controller;

import com.example.OnlyBuns.dto.ChatMessageDto;
import com.example.OnlyBuns.dto.MessageSendRequestDto;
import com.example.OnlyBuns.service.ChatMessageService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatMessageController {
    private final ChatMessageService chatMessageService;

    public ChatMessageController(ChatMessageService chatMessageService) {
        this.chatMessageService = chatMessageService;
    }

    @PostMapping("/messages")
    public ResponseEntity<ChatMessageDto> sendMessage(
            @RequestBody MessageSendRequestDto request,
            Principal principal) {
        try {
            Integer senderId = Integer.parseInt(principal.getName());
            ChatMessageDto sentMessage = chatMessageService.sendMessage(request, senderId);
            return new ResponseEntity<>(sentMessage, HttpStatus.CREATED); // CREATED jer je poruka nova
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // Soba ili pošiljalac ne postoje
        } catch (SecurityException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN); // Pošiljalac nije član sobe
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // Loši ulazni podaci
        }
    }
    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<List<ChatMessageDto>> getChatHistory(
            @PathVariable Integer roomId,
            Principal principal) {
        try {
            Integer userId = Integer.parseInt(principal.getName());
            List<ChatMessageDto> messages = chatMessageService.getChatHistory(roomId, userId);
            return new ResponseEntity<>(messages, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (SecurityException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN); // Korisnik nije član sobe
        } catch (NumberFormatException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }
}
