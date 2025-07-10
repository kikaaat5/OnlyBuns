package com.example.OnlyBuns.controller;

import com.example.OnlyBuns.dto.ChatRoomCreateRequestDto;
import com.example.OnlyBuns.dto.ChatRoomDto;
import com.example.OnlyBuns.model.Client;
import com.example.OnlyBuns.service.ChatRoomService;
import com.example.OnlyBuns.service.ClientService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/chat/rooms")
public class ChatRoomController {

    private static final Logger logger = LoggerFactory.getLogger(ChatRoomController.class);

    private final ChatRoomService chatRoomService;
    private final ClientService clientService;

    public ChatRoomController(ChatRoomService chatRoomService, ClientService clientService) {
        this.chatRoomService = chatRoomService;
        this.clientService = clientService;
    }

    private Integer getCurrentClientId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new IllegalStateException("Korisnik nije autentifikovan. Prijavite se da biste izvršili ovu operaciju.");
        }

        String username = authentication.getName();

        Client client = clientService.findByUsername(username);

        if (client != null) {
            return Math.toIntExact(client.getId()); // Vraća ID klijenta
        } else {
            throw new IllegalStateException("Ulogovani klijent sa emailom '" + username + "' nije pronađen u bazi podataka.");
        }
    }

    @PostMapping("/private/{otherUserId}")
    public ResponseEntity<Integer> getOrCreatePrivateChat(
            @PathVariable Integer otherUserId,
            Principal principal) {
        try {
            Integer currentUserId = getCurrentClientId();

            if (currentUserId.equals(otherUserId)) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            Integer chatRoomId = chatRoomService.getOrCreatePrivateChatRoom(currentUserId, otherUserId);
            return new ResponseEntity<>(chatRoomId, HttpStatus.OK);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/group")
    public ResponseEntity<ChatRoomDto> createGroupChat(
            @RequestBody ChatRoomCreateRequestDto request,
            Principal principal) {
        logger.info("Request to create group chat: {}", request.getName());
        try {
            Integer adminId = getCurrentClientId();
            ChatRoomDto newChatRoom = chatRoomService.createGroupChatRoom(request, adminId);
            return new ResponseEntity<>(newChatRoom, HttpStatus.CREATED);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<List<ChatRoomDto>> getMyChatRooms(Principal principal) {
        logger.info("Request for getMyChatRooms");
        try {
            Integer userId = getCurrentClientId();
            List<ChatRoomDto> chatRooms = chatRoomService.getChatRoomsForUser(userId);
            return new ResponseEntity<>(chatRooms, HttpStatus.OK);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<ChatRoomDto> getChatRoomDetails(
            @PathVariable Integer roomId,
            Principal principal) {
        try {
            Integer userId = getCurrentClientId();
            ChatRoomDto chatRoom = chatRoomService.getChatRoomDetails(roomId, userId);
            return new ResponseEntity<>(chatRoom, HttpStatus.OK);
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

    @PostMapping("/{roomId}/members/{newMemberId}")
    public ResponseEntity<Void> addMemberToGroupChat(
            @PathVariable Integer roomId,
            @PathVariable Integer newMemberId,
            Principal principal) {
        try {
            Integer currentUserId = getCurrentClientId();
            chatRoomService.addMemberToGroupChat(roomId, newMemberId, currentUserId);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (SecurityException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{roomId}/members/{memberToRemoveId}")
    public ResponseEntity<Void> removeMemberFromGroupChat(
            @PathVariable Integer roomId,
            @PathVariable Integer memberToRemoveId,
            Principal principal) {
        try {
            Integer currentUserId = getCurrentClientId();
            chatRoomService.removeMemberFromGroupChat(roomId, memberToRemoveId, currentUserId);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (SecurityException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}