package com.example.OnlyBuns.controller;

import com.example.OnlyBuns.dto.ChatRoomCreateRequestDto;
import com.example.OnlyBuns.dto.ChatRoomDto;
import com.example.OnlyBuns.model.Client; // Dodaj import za Client entitet
import com.example.OnlyBuns.service.ChatRoomService;
import com.example.OnlyBuns.service.ClientService; // Dodaj import za ClientService
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication; // Dodaj import
import org.springframework.security.core.context.SecurityContextHolder; // Dodaj import
import org.springframework.web.bind.annotation.*;

import java.security.Principal; // Može ostati, ali ga nećemo direktno koristiti za ID
import java.util.List;
import org.slf4j.Logger; // Dodaj import
import org.slf4j.LoggerFactory; // Dodaj import

@RestController
@RequestMapping("/api/chat/rooms")
public class ChatRoomController { // Preimenovao sam u ChatRoomController, ako je to tvoje ime klase

    private static final Logger logger = LoggerFactory.getLogger(ChatRoomController.class); // Inicijalizuj logger

    private final ChatRoomService chatRoomService;
    private final ClientService clientService; // NOVO: Injektuj ClientService

    // Ažuriraj konstruktor da prima ClientService
    public ChatRoomController(ChatRoomService chatRoomService, ClientService clientService) {
        this.chatRoomService = chatRoomService;
        this.clientService = clientService; // Inicijalizuj ClientService
    }

    // KOPIRANA I PRILAGOĐENA METODA IZ FollowRelationController
    // Ova metoda dohvaća ID ulogovanog klijenta iz Spring Security konteksta
    private Integer getCurrentClientId() { // Promenjeno u Integer da bude konzistentno
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            logger.warn("getCurrentClientId: User not authenticated or is anonymous. Authentication: {}", authentication);
            throw new IllegalStateException("Korisnik nije autentifikovan. Prijavite se da biste izvršili ovu operaciju.");
        }

        String username = authentication.getName(); // Ovo vraća username (npr. "jana", "ana")
        logger.debug("getCurrentClientId: Authenticated username from Principal: {}", username);

        // Dohvati Client entitet iz baze na osnovu username-a
        Client client = clientService.findByUsername(username);

        if (client != null) {
            logger.debug("getCurrentClientId: Found client ID: {} for username: {}", client.getId(), username);
            return Math.toIntExact(client.getId()); // Vraća ID klijenta
        } else {
            logger.error("getCurrentClientId: Logged in client with username '{}' not found in database. This indicates a data inconsistency.", username);
            throw new IllegalStateException("Ulogovani klijent sa emailom '" + username + "' nije pronađen u bazi podataka.");
        }
    }

    @PostMapping("/private/{otherUserId}")
    public ResponseEntity<Integer> getOrCreatePrivateChat(
            @PathVariable Integer otherUserId,
            Principal principal) { // Principal je ovde više za informativne logove, ne koristi se direktno za ID
        logger.info("Request to get or create private chat with otherUserId: {}", otherUserId);
        try {
            // Dohvati ID trenutno ulogovanog korisnika koristeći pomoćnu metodu
            Integer currentUserId = getCurrentClientId();

            // Spriječi korisnika da kreira privatni chat sam sa sobom
            if (currentUserId.equals(otherUserId)) {
                logger.warn("Attempt to create private chat with self by user ID: {}", currentUserId);
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // Status 400 Bad Request
            }

            Integer chatRoomId = chatRoomService.getOrCreatePrivateChatRoom(currentUserId, otherUserId);
            logger.info("Private chat room ID {} retrieved/created between user {} and {}", chatRoomId, currentUserId, otherUserId);
            return new ResponseEntity<>(chatRoomId, HttpStatus.OK); // OK jer može vratiti postojeći
        } catch (IllegalStateException e) { // Hvata greške iz getCurrentClientId()
            logger.error("Authentication/Authorization error in getOrCreatePrivateChat: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); // Status 401 Unauthorized
        } catch (EntityNotFoundException e) {
            logger.error("Entity not found during getOrCreatePrivateChat: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // Status 404 Not Found
        } catch (IllegalArgumentException e) {
            logger.error("Invalid argument in getOrCreatePrivateChat: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // Status 400 Bad Request
        } catch (Exception e) {
            logger.error("An unexpected error occurred in getOrCreatePrivateChat: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR); // Status 500 Internal Server Error
        }
    }

    @PostMapping("/group")
    public ResponseEntity<ChatRoomDto> createGroupChat(
            @RequestBody ChatRoomCreateRequestDto request,
            Principal principal) { // Principal je ovde više za informativne logove
        logger.info("Request to create group chat: {}", request.getName());
        try {
            // Dohvati ID admina grupe (trenutno ulogovanog korisnika)
            Integer adminId = getCurrentClientId();
            logger.info("Creating group chat '{}' by admin ID: {}", request.getName(), adminId);
            ChatRoomDto newChatRoom = chatRoomService.createGroupChatRoom(request, adminId);
            logger.info("Group chat '{}' created with ID: {}", newChatRoom.getName(), newChatRoom.getId());
            return new ResponseEntity<>(newChatRoom, HttpStatus.CREATED); // Status 201 Created
        } catch (IllegalStateException e) { // Hvata greške iz getCurrentClientId()
            logger.error("Authentication/Authorization error in createGroupChat: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); // Status 401 Unauthorized
        } catch (EntityNotFoundException e) {
            logger.error("Entity not found during createGroupChat: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // Status 404 Not Found
        } catch (IllegalArgumentException e) {
            logger.error("Invalid argument in createGroupChat: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // Status 400 Bad Request
        } catch (Exception e) {
            logger.error("An unexpected error occurred in createGroupChat: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR); // Status 500 Internal Server Error
        }
    }

    @GetMapping("/me")
    public ResponseEntity<List<ChatRoomDto>> getMyChatRooms(Principal principal) { // Principal je ovde više za informativne logove
        logger.info("Request for getMyChatRooms");
        try {
            // Dohvati ID trenutno ulogovanog korisnika
            Integer userId = getCurrentClientId();
            logger.info("Fetching chat rooms for user ID: {}", userId);
            List<ChatRoomDto> chatRooms = chatRoomService.getChatRoomsForUser(userId);
            logger.info("Successfully fetched {} chat rooms for user ID: {}", chatRooms.size(), userId);
            return new ResponseEntity<>(chatRooms, HttpStatus.OK); // Status 200 OK
        } catch (IllegalStateException e) { // Hvata greške iz getCurrentClientId()
            logger.error("Authentication/Authorization error in getMyChatRooms: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); // Status 401 Unauthorized
        } catch (EntityNotFoundException e) {
            logger.error("Entity not found in getMyChatRooms: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // Status 404 Not Found
        } catch (Exception e) {
            logger.error("An unexpected error occurred in getMyChatRooms: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR); // Status 500 Internal Server Error
        }
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<ChatRoomDto> getChatRoomDetails(
            @PathVariable Integer roomId,
            Principal principal) { // Principal je ovde više za informativne logove
        logger.info("Request for chat room details for roomId: {}", roomId);
        try {
            // Dohvati ID trenutno ulogovanog korisnika
            Integer userId = getCurrentClientId();
            logger.info("Fetching details for chat room {} for user ID: {}", roomId, userId);
            ChatRoomDto chatRoom = chatRoomService.getChatRoomDetails(roomId, userId);
            logger.info("Successfully fetched details for chat room {}", roomId);
            return new ResponseEntity<>(chatRoom, HttpStatus.OK); // Status 200 OK
        } catch (IllegalStateException e) { // Hvata greške iz getCurrentClientId()
            logger.error("Authentication/Authorization error in getChatRoomDetails: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); // Status 401 Unauthorized
        } catch (EntityNotFoundException e) {
            logger.error("Entity not found in getChatRoomDetails: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // Status 404 Not Found
        } catch (SecurityException e) {
            logger.warn("Security violation in getChatRoomDetails for room {}: {}", roomId, e.getMessage());
            return new ResponseEntity<>(HttpStatus.FORBIDDEN); // Status 403 Forbidden (Korisnik nije član)
        } catch (Exception e) {
            logger.error("An unexpected error occurred in getChatRoomDetails: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR); // Status 500 Internal Server Error
        }
    }

    @PostMapping("/{roomId}/members/{newMemberId}")
    public ResponseEntity<Void> addMemberToGroupChat(
            @PathVariable Integer roomId,
            @PathVariable Integer newMemberId,
            Principal principal) { // Principal je ovde više za informativne logove
        logger.info("Request to add member {} to room {}", newMemberId, roomId);
        try {
            // Dohvati ID trenutno ulogovanog korisnika
            Integer currentUserId = getCurrentClientId();
            logger.info("User {} attempting to add member {} to room {}", currentUserId, newMemberId, roomId);
            chatRoomService.addMemberToGroupChat(roomId, newMemberId, currentUserId);
            logger.info("Member {} successfully added to room {}", newMemberId, roomId);
            return new ResponseEntity<>(HttpStatus.OK); // Status 200 OK
        } catch (IllegalStateException e) { // Hvata greške iz getCurrentClientId()
            logger.error("Authentication/Authorization error in addMemberToGroupChat: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); // Status 401 Unauthorized
        } catch (EntityNotFoundException e) {
            logger.error("Entity not found in addMemberToGroupChat: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // Status 404 Not Found
        } catch (SecurityException e) {
            logger.warn("Security violation in addMemberToGroupChat for room {}: {}", roomId, e.getMessage());
            return new ResponseEntity<>(HttpStatus.FORBIDDEN); // Status 403 Forbidden (Nije admin)
        } catch (IllegalArgumentException e) {
            logger.error("Invalid argument in addMemberToGroupChat: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // Status 400 Bad Request (Npr. pokušaj dodavanja u privatni čet)
        } catch (Exception e) {
            logger.error("An unexpected error occurred in addMemberToGroupChat: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR); // Status 500 Internal Server Error
        }
    }

    @DeleteMapping("/{roomId}/members/{memberToRemoveId}")
    public ResponseEntity<Void> removeMemberFromGroupChat(
            @PathVariable Integer roomId,
            @PathVariable Integer memberToRemoveId,
            Principal principal) { // Principal je ovde više za informativne logove
        logger.info("Request to remove member {} from room {}", memberToRemoveId, roomId);
        try {
            // Dohvati ID trenutno ulogovanog korisnika
            Integer currentUserId = getCurrentClientId();
            logger.info("User {} attempting to remove member {} from room {}", currentUserId, memberToRemoveId, roomId);
            chatRoomService.removeMemberFromGroupChat(roomId, memberToRemoveId, currentUserId);
            logger.info("Member {} successfully removed from room {}", memberToRemoveId, roomId);
            return new ResponseEntity<>(HttpStatus.OK); // Status 200 OK
        } catch (IllegalStateException e) { // Hvata greške iz getCurrentClientId()
            logger.error("Authentication/Authorization error in removeMemberFromGroupChat: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); // Status 401 Unauthorized
        } catch (EntityNotFoundException e) {
            logger.error("Entity not found in removeMemberFromGroupChat: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // Status 404 Not Found
        } catch (SecurityException e) {
            logger.warn("Security violation in removeMemberFromGroupChat for room {}: {}", roomId, e.getMessage());
            return new ResponseEntity<>(HttpStatus.FORBIDDEN); // Status 403 Forbidden (Nije admin)
        } catch (IllegalArgumentException e) {
            logger.error("Invalid argument in removeMemberFromGroupChat: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // Status 400 Bad Request (Npr. pokušaj uklanjanja iz privatnog četa)
        } catch (Exception e) {
            logger.error("An unexpected error occurred in removeMemberFromGroupChat: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR); // Status 500 Internal Server Error
        }
    }
}