package com.example.OnlyBuns.controller;


import com.example.OnlyBuns.dto.ChatRoomCreateRequestDto;
import com.example.OnlyBuns.dto.ChatRoomDto;
import com.example.OnlyBuns.service.ChatRoomService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/chat/rooms")
public class ChatRoomController {
    private final ChatRoomService chatRoomService;

    public ChatRoomController(ChatRoomService chatRoomService) {
        this.chatRoomService = chatRoomService;
    }

    @PostMapping("/private/{otherUserId}")
    public ResponseEntity<Integer> getOrCreatePrivateChat(
            @PathVariable Integer otherUserId,
            Principal principal) { // Principal sadrži ime ulogovanog korisnika (obično username/ID)
        try {
            // Pretpostavka: principal.getName() vraća String reprezentaciju ID-ja ulogovanog korisnika.
            // U realnoj aplikaciji, možda bi trebalo da ga parsiraš ili da koristiš @AuthenticationPrincipal za custom UserDetails objekat.
            Integer currentUserId = Integer.parseInt(principal.getName()); // Pretpostavljamo da name sadrži user ID

            // Spriječi korisnika da kreira privatni chat sam sa sobom
            if (currentUserId.equals(otherUserId)) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            Integer chatRoomId = chatRoomService.getOrCreatePrivateChatRoom(currentUserId, otherUserId);
            return new ResponseEntity<>(chatRoomId, HttpStatus.OK); // OK jer može vratiti postojeći
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (SecurityException | IllegalArgumentException e) {
            // Npr. ako currentUserId ne postoji ili ako se pokuša privatni čet sa nepostojećim drugim korisnikom
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/group")
    public ResponseEntity<ChatRoomDto> createGroupChat(
           @RequestBody ChatRoomCreateRequestDto request,
            Principal principal) {
        try {
            Integer adminId = Integer.parseInt(principal.getName()); // Admin je trenutno ulogovan korisnik
            ChatRoomDto newChatRoom = chatRoomService.createGroupChatRoom(request, adminId);
            return new ResponseEntity<>(newChatRoom, HttpStatus.CREATED);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
    @GetMapping("/me")
    public ResponseEntity<List<ChatRoomDto>> getMyChatRooms(Principal principal) {
        try {
            Integer userId = Integer.parseInt(principal.getName());
            List<ChatRoomDto> chatRooms = chatRoomService.getChatRoomsForUser(userId);
            return new ResponseEntity<>(chatRooms, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (NumberFormatException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }
    @GetMapping("/{roomId}")
    public ResponseEntity<ChatRoomDto> getChatRoomDetails(
            @PathVariable Integer roomId,
            Principal principal) {
        try {
            Integer userId = Integer.parseInt(principal.getName());
            ChatRoomDto chatRoom = chatRoomService.getChatRoomDetails(roomId, userId);
            return new ResponseEntity<>(chatRoom, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (SecurityException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN); // Korisnik nije član
        } catch (NumberFormatException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/{roomId}/members/{newMemberId}")
    public ResponseEntity<Void> addMemberToGroupChat(
            @PathVariable Integer roomId,
            @PathVariable Integer newMemberId,
            Principal principal) {
        try {
            Integer currentUserId = Integer.parseInt(principal.getName());
            chatRoomService.addMemberToGroupChat(roomId, newMemberId, currentUserId);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (SecurityException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN); // Nije admin
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // Npr. pokušaj dodavanja u privatni čet
        }
    }
    @DeleteMapping("/{roomId}/members/{memberToRemoveId}")
    public ResponseEntity<Void> removeMemberFromGroupChat(
            @PathVariable Integer roomId,
            @PathVariable Integer memberToRemoveId,
            Principal principal) {
        try {
            Integer currentUserId = Integer.parseInt(principal.getName());
            chatRoomService.removeMemberFromGroupChat(roomId, memberToRemoveId, currentUserId);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (SecurityException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN); // Nije admin
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // Npr. pokušaj uklanjanja iz privatnog četa
        }
    }
}
