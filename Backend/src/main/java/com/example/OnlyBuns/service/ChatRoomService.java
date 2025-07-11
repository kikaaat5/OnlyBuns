package com.example.OnlyBuns.service;

import com.example.OnlyBuns.dto.ChatRoomCreateRequestDto;
import com.example.OnlyBuns.dto.ChatRoomDto;
import com.example.OnlyBuns.dto.ClientDto;
import com.example.OnlyBuns.model.Client;
import com.example.OnlyBuns.model.ChatRoom;
import com.example.OnlyBuns.model.ChatRoomType;
import com.example.OnlyBuns.model.ChatRoomMember;
import com.example.OnlyBuns.model.MemberRole;
import com.example.OnlyBuns.repository.ChatRoomMemberRepository;
import com.example.OnlyBuns.repository.ChatRoomRepository;
import com.example.OnlyBuns.repository.ClientRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@Service

public class ChatRoomService {
    private static final Logger logger = LoggerFactory.getLogger(ChatRoomService.class);
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ClientRepository clientRepository;

    public ChatRoomService(ChatRoomRepository chatRoomRepository,
                           ChatRoomMemberRepository chatRoomMemberRepository,
                           ClientRepository clientRepository,
                           SimpMessagingTemplate messagingTemplate) {
        this.chatRoomRepository = chatRoomRepository;
        this.chatRoomMemberRepository = chatRoomMemberRepository;
        this.clientRepository = clientRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public Integer getOrCreatePrivateChatRoom(Integer user1Id, Integer user2Id) {

        Client client1 = clientRepository.findById(user1Id)
                .orElseThrow(() -> new EntityNotFoundException("Client not found with ID: " + user1Id));
        Client client2 = clientRepository.findById(user2Id)
                .orElseThrow(() -> new EntityNotFoundException("Client not found with ID: " + user2Id));

        Optional<ChatRoom> existingRoom = chatRoomMemberRepository.findPrivateChatRoomByTwoMembers(user1Id, user2Id);

        if (existingRoom.isEmpty()) {
            existingRoom = chatRoomMemberRepository.findPrivateChatRoomByTwoMembers(user2Id, user1Id);
        }

        if (existingRoom.isPresent()) {
            return existingRoom.get().getId();
        }

        ChatRoom newRoom = new ChatRoom();
        newRoom.setType(ChatRoomType.PRIVATE);
        newRoom = chatRoomRepository.save(newRoom);

        ChatRoomMember member1 = new ChatRoomMember();
        member1.setClient(client1);
        member1.setChatRoom(newRoom);
        member1.setRole(MemberRole.MEMBER);
        chatRoomMemberRepository.save(member1);

        ChatRoomMember member2 = new ChatRoomMember();
        member2.setClient(client2);
        member2.setChatRoom(newRoom);
        member2.setRole(MemberRole.MEMBER);
        chatRoomMemberRepository.save(member2);

        if (newRoom.getMembers() == null) {
            newRoom.setMembers(new ArrayList<>());
        }
        newRoom.getMembers().add(member1);
        newRoom.getMembers().add(member2);
        ChatRoomDto chatRoomDto = mapChatRoomToDTO(newRoom);

        if (client1.getUsername() != null) {
            messagingTemplate.convertAndSendToUser(
                    client1.getUsername(),
                    "/queue/chat-rooms",
                    chatRoomDto
            );
            logger.info("Sent private chat update to {}: Room ID={}", client1.getUsername(), newRoom.getId());
        } else {
            logger.warn("Client 1 (ID: {}) has no username, cannot send WebSocket update.", client1.getId());
        }

        if (client2.getUsername() != null) {
            messagingTemplate.convertAndSendToUser(
                    client2.getUsername(), // Username primaoca
                    "/queue/chat-rooms",
                    chatRoomDto
            );
            logger.info("Sent private chat update to {}: Room ID={}", client2.getUsername(), newRoom.getId());
        } else {
            logger.warn("Client 2 (ID: {}) has no username, cannot send WebSocket update.", client2.getId());
        }
        return newRoom.getId();
    }

    @Transactional
    public ChatRoomDto createGroupChatRoom(ChatRoomCreateRequestDto request, Integer adminId) {
        Client admin = clientRepository.findById(adminId)
                .orElseThrow(() -> new EntityNotFoundException("Admin not found with ID: " + adminId));

        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setName(request.getName());
        chatRoom.setType(ChatRoomType.GROUP);
        chatRoom.setAdmin(admin);
        chatRoom = chatRoomRepository.save(chatRoom);

        ChatRoomMember adminMember = new ChatRoomMember();
        adminMember.setClient(admin);
        adminMember.setChatRoom(chatRoom);
        adminMember.setRole(MemberRole.ADMIN);
        chatRoomMemberRepository.save(adminMember);
        chatRoom.getMembers().add(adminMember);

        for (Integer memberId : request.getMemberIds()) {
            Client memberClient = clientRepository.findById(memberId)
                    .orElseThrow(() -> new EntityNotFoundException("Member not found with ID: " + memberId));
            if (chatRoomMemberRepository.findByClientAndChatRoom(memberClient, chatRoom).isEmpty()) {
                ChatRoomMember member = new ChatRoomMember();
                member.setClient(memberClient);
                member.setChatRoom(chatRoom);
                member.setRole(MemberRole.MEMBER);
                member.setJoinedAt(LocalDateTime.now());
                chatRoomMemberRepository.save(member);
                chatRoom.getMembers().add(member);
            }
        }

        ChatRoomDto chatRoomDto = mapChatRoomToDTO(chatRoom);
        for (ChatRoomMember member : chatRoom.getMembers()) {
            if (member.getClient() != null && member.getClient().getUsername() != null) {
                messagingTemplate.convertAndSendToUser(
                        member.getClient().getUsername(),
                        "/queue/chat-rooms",
                        chatRoomDto
                );
                logger.info("Sent group chat creation update to {}: Room ID={}", member.getClient().getUsername(), chatRoom.getId());
            } else {
                logger.warn("Member (ID: {}) in chat room (ID: {}) has no client or username, cannot send WebSocket update.", member.getId(), chatRoom.getId());
            }
        }
        return mapChatRoomToDTO(chatRoom);
    }

    @Transactional(readOnly = true) // Transakcija mora biti aktivna!
    public List<ChatRoomDto> getChatRoomsForUser(Integer userId) {
        List<ChatRoom> chatRooms = chatRoomRepository.findChatRoomsByClientId(userId);

        return chatRooms.stream()
                .map(chatRoom -> {

                    if (chatRoom.getAdmin() != null) {
                        logger.debug("  Admin Entity in ChatRoom {}: ID={}, Username='{}', Email='{}'",
                                chatRoom.getId(), chatRoom.getAdmin().getId(), chatRoom.getAdmin().getUsername(), chatRoom.getAdmin().getEmail());
                    } else {
                        logger.debug("  ChatRoom {} has no admin entity.", chatRoom.getId());
                    }

                    if (chatRoom.getMembers() != null) {
                        logger.debug("  ChatRoom {} has {} members in entity collection (lazy loaded):", chatRoom.getId(), chatRoom.getMembers().size());
                        chatRoom.getMembers().forEach(member -> {
                            if (member.getClient() != null) {
                                logger.debug("    Member Entity: Client ID={}, Username='{}', Email='{}'",
                                        member.getClient().getId(), member.getClient().getUsername(), member.getClient().getEmail());
                            } else {
                                logger.warn("    Member found with null Client entity for ChatRoom ID: {}", chatRoom.getId());
                            }
                        });
                    } else {
                        logger.warn("  ChatRoom {} members list is null (should not happen if mapped correctly).", chatRoom.getId());
                    }

                    ChatRoomDto dto = mapChatRoomToDTO(chatRoom);
                    logger.debug("--- Finished mapping ChatRoom Entity ID: {} to DTO ---", chatRoom.getId());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ChatRoomDto getChatRoomDetails(Integer chatRoomId, Integer userId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new EntityNotFoundException("Chat Room not found with ID: " + chatRoomId));

        Client user = clientRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Client not found with ID: " + userId));

        boolean isMember = chatRoomMemberRepository.findByClientAndChatRoom(user, chatRoom).isPresent();
        if (!isMember) {
            throw new SecurityException("User is not a member of this chat room.");
        }

        return mapChatRoomToDTO(chatRoom);
    }

    @Transactional(readOnly = true)
    public ChatRoom findChatRoomById(Integer chatRoomId) {
        return chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new EntityNotFoundException("Chat Room not found with ID: " + chatRoomId));
    }


    @Transactional
    public void addMemberToGroupChat(Integer chatRoomId, Integer newMemberId, Integer currentUserId) {
        ChatRoom chatRoom = findChatRoomById(chatRoomId);
        if (chatRoom.getType() == ChatRoomType.PRIVATE) {
            throw new IllegalArgumentException("Cannot add members to a private chat.");
        }

        Client newMember = clientRepository.findById(newMemberId)
                .orElseThrow(() -> new EntityNotFoundException("New member not found with ID: " + newMemberId));

        if (chatRoomMemberRepository.findByClientAndChatRoom(newMember, chatRoom).isPresent()) {
            throw new IllegalArgumentException("User is already a member of this chat room.");
        }

        ChatRoomMember member = new ChatRoomMember();
        member.setClient(newMember);
        member.setChatRoom(chatRoom);
        member.setRole(MemberRole.MEMBER);
        member.setJoinedAt(LocalDateTime.now());
        chatRoomMemberRepository.save(member);
        chatRoom.getMembers().add(member); // Ažuriraj listu u entitetu
    }

    @Transactional
    public void removeMemberFromGroupChat(Integer chatRoomId, Integer memberToRemoveId, Integer currentUserId) {
        ChatRoom chatRoom = findChatRoomById(chatRoomId);
        if (chatRoom.getType() == ChatRoomType.PRIVATE) {
            throw new IllegalArgumentException("Cannot remove members from a private chat.");
        }

        Client memberClient = clientRepository.findById(memberToRemoveId)
                .orElseThrow(() -> new EntityNotFoundException("Member to remove not found with ID: " + memberToRemoveId));

        if (chatRoom.getAdmin().getId().equals(memberToRemoveId)) {
            throw new IllegalArgumentException("Admin cannot remove themselves. Transfer admin role first.");
        }

        ChatRoomMember membership = chatRoomMemberRepository.findByClientAndChatRoom(memberClient, chatRoom)
                .orElseThrow(() -> new EntityNotFoundException("Member is not part of this chat room."));

        chatRoom.getMembers().remove(membership);
        chatRoomMemberRepository.delete(membership);
    }

    private ChatRoomDto mapChatRoomToDTO(ChatRoom chatRoom) {
        ChatRoomDto dto = new ChatRoomDto();
        dto.setId(chatRoom.getId());
        dto.setName(chatRoom.getName());
        dto.setType(chatRoom.getType());

        if (chatRoom.getAdmin() != null) {
            dto.setAdmin(mapClientToDTO(chatRoom.getAdmin()));
            logger.debug("  Mapped admin for ChatRoom ID {}: Client ID {}, Username: {}", chatRoom.getId(), dto.getAdmin().getId(), dto.getAdmin().getUsername());
        } else {
            logger.debug("  ChatRoom ID {} has no admin (likely a private chat).", chatRoom.getId());
        }

        if (chatRoom.getMembers() != null) {
            List<ClientDto> memberDtos = chatRoom.getMembers().stream()
                    .map(member -> {
                        ClientDto clientDto = mapClientToDTO(member.getClient());
                        logger.debug("  Mapping member Client ID {} ({}) to DTO for ChatRoom ID {}", clientDto.getId(), clientDto.getUsername(), chatRoom.getId());
                        return clientDto;
                    })
                    .collect(Collectors.toList());
            dto.setMembers(memberDtos);
        } else {
            dto.setMembers(new ArrayList<>());
            logger.warn("  ChatRoom ID {} members list is null during DTO mapping. Setting to empty list.", chatRoom.getId());
        }

        return dto;
    }

    private ClientDto mapClientToDTO(Client client) {
        if (client == null) {
            return null;
        }

        if (client.getId() == null || client.getUsername() == null) {
            return new ClientDto(null, null);
        }
        return new ClientDto(client.getId(), client.getUsername());
    }
}
