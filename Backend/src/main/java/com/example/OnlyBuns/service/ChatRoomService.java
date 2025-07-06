package com.example.OnlyBuns.service;

import com.example.OnlyBuns.dto.ChatRoomCreateRequestDto;
import com.example.OnlyBuns.dto.ChatRoomDto;
import com.example.OnlyBuns.dto.ClientDto;
import com.example.OnlyBuns.model.Client;
import com.example.OnlyBuns.model.ChatRoom;
import com.example.OnlyBuns.model.ChatRoomType;
import com.example.OnlyBuns.model.ChatRoomMember;
import com.example.OnlyBuns.model.ChatRoomType;
import com.example.OnlyBuns.model.MemberRole;
import com.example.OnlyBuns.repository.ChatRoomMemberRepository;
import com.example.OnlyBuns.repository.ChatRoomRepository;
import com.example.OnlyBuns.repository.ClientRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.List;
import org.slf4j.Logger; // Dodaj import
import org.slf4j.LoggerFactory;

@Service

public class ChatRoomService {
    private static final Logger logger = LoggerFactory.getLogger(ChatRoomService.class);
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ClientRepository clientRepository;

    public ChatRoomService(ChatRoomRepository chatRoomRepository,
                           ChatRoomMemberRepository chatRoomMemberRepository,
                           ClientRepository clientRepository) {
        this.chatRoomRepository = chatRoomRepository;
        this.chatRoomMemberRepository = chatRoomMemberRepository;
        this.clientRepository = clientRepository;
    }

    @Transactional
    public Integer getOrCreatePrivateChatRoom(Integer user1Id, Integer user2Id) {
        System.out.println("--- getOrCreatePrivateChatRoom START ---");
        System.out.println("Attempting to get or create private chat between user IDs: " + user1Id + " and " + user2Id);

        Client client1 = clientRepository.findById(user1Id)
                .orElseThrow(() -> new EntityNotFoundException("Client not found with ID: " + user1Id));
        Client client2 = clientRepository.findById(user2Id)
                .orElseThrow(() -> new EntityNotFoundException("Client not found with ID: " + user2Id));

        Optional<ChatRoom> existingRoom = chatRoomMemberRepository.findPrivateChatRoomByTwoMembers(user1Id, user2Id);

        if (existingRoom.isEmpty()) {
            existingRoom = chatRoomMemberRepository.findPrivateChatRoomByTwoMembers(user2Id, user1Id);
        }

        if (existingRoom.isPresent()) {
            System.out.println("    >>> FOUND EXISTING PRIVATE CHAT via custom query: " + existingRoom.get().getId() + " <<<");
            return existingRoom.get().getId();
        }

        System.out.println("No existing private chat found between " + user1Id + " and " + user2Id + ". Creating a new one.");

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

        System.out.println("--- New private chat created with ID: " + newRoom.getId() + " ---");
        System.out.println("--- getOrCreatePrivateChatRoom END ---");
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
        chatRoom = chatRoomRepository.save(chatRoom); // Sačuvaj sobu da dobije ID

        // Dodaj admina kao člana
        ChatRoomMember adminMember = new ChatRoomMember();
        adminMember.setClient(admin);
        adminMember.setChatRoom(chatRoom);
        adminMember.setRole(MemberRole.ADMIN);
        chatRoomMemberRepository.save(adminMember);
        chatRoom.getMembers().add(adminMember); // Dodaj u listu entiteta

        // Dodaj ostale članove
        for (Integer memberId : request.getMemberIds()) {
            Client memberClient = clientRepository.findById(memberId)
                    .orElseThrow(() -> new EntityNotFoundException("Member not found with ID: " + memberId));
            // Proveri da li član već postoji
            if (chatRoomMemberRepository.findByClientAndChatRoom(memberClient, chatRoom).isEmpty()) {
                ChatRoomMember member = new ChatRoomMember();
                member.setClient(memberClient);
                member.setChatRoom(chatRoom);
                member.setRole(MemberRole.MEMBER); // Svi su MEMBER osim eksplicitnog admina
                chatRoomMemberRepository.save(member);
                chatRoom.getMembers().add(member); // Dodaj u listu entiteta
            }
        }

        return mapChatRoomToDTO(chatRoom);
    }

   /* @Transactional(readOnly = true)
    public List<ChatRoomDto> getChatRoomsForUser(Integer userId) {
        Client user = clientRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Client not found with ID: " + userId));

        // Pronađi sva članstva za datog korisnika
        List<ChatRoom> chatRooms = chatRoomRepository.findChatRoomsByClientWithMembers(userId); // Ovi ChatRoom objekti su EAGERLY LOADED sa članovima
        return chatRooms.stream()
                .map(this::mapChatRoomToDTO) // mapChatRoomToDTO sada sigurno ima dostupne članove
                .collect(Collectors.toList());
    }*/

    /*@Transactional(readOnly = true)
    public List<ChatRoomDto> getChatRoomsForUser(Integer userId) {
        logger.info("Fetching chat rooms for user ID: {}", userId);
        List<ChatRoom> chatRooms = chatRoomRepository.findChatRoomsByClientWithMembers(userId);
        logger.info("Found {} chat room entities for user ID: {}", chatRooms.size(), userId);

        return chatRooms.stream()
                .map(chatRoom -> {
                    logger.debug("--- Processing ChatRoom Entity ID: {} (Type: {}) ---", chatRoom.getId(), chatRoom.getType());

                    // Logovi za admina entiteta
                    if (chatRoom.getAdmin() != null) {
                        logger.debug("  Admin Entity in ChatRoom {}: ID={}, Username='{}', Email='{}'",
                                chatRoom.getId(), chatRoom.getAdmin().getId(), chatRoom.getAdmin().getUsername(), chatRoom.getAdmin().getEmail());
                    } else {
                        logger.debug("  ChatRoom {} has no admin entity.", chatRoom.getId());
                    }

                    // Logovi za članove entiteta
                    if (chatRoom.getMembers() != null && !chatRoom.getMembers().isEmpty()) {
                        logger.debug("  ChatRoom {} has {} members in entity collection:", chatRoom.getId(), chatRoom.getMembers().size());
                        chatRoom.getMembers().forEach(member -> {
                            if (member.getClient() != null) {
                                logger.debug("    Member Entity: Client ID={}, Username='{}', Email='{}'",
                                        member.getClient().getId(), member.getClient().getUsername(), member.getClient().getEmail());
                            } else {
                                logger.warn("    Member found with null Client entity for ChatRoom ID: {}", chatRoom.getId());
                            }
                        });
                    } else {
                        logger.warn("  ChatRoom {} has no members loaded or members list is null (after JOIN FETCH).", chatRoom.getId());
                    }

                    // Sada mapiraj u DTO
                    ChatRoomDto dto = mapChatRoomToDTO(chatRoom);
                    logger.debug("--- Finished mapping ChatRoom Entity ID: {} to DTO ---", chatRoom.getId());
                    return dto;
                })
                .collect(Collectors.toList());
    }*/

    @Transactional(readOnly = true) // Transakcija mora biti aktivna!
    public List<ChatRoomDto> getChatRoomsForUser(Integer userId) {
        logger.info("Fetching chat rooms for user ID: {}", userId);
        // KLJUČNO: Koristi novu metodu koja samo dohvaća ChatRoom entitete
        List<ChatRoom> chatRooms = chatRoomRepository.findChatRoomsByClientId(userId);
        logger.info("Found {} chat room entities for user ID: {}", chatRooms.size(), userId);

        return chatRooms.stream()
                .map(chatRoom -> {
                    logger.debug("--- Processing ChatRoom Entity ID: {} (Type: {}) ---", chatRoom.getId(), chatRoom.getType());

                    // Sada, kada se pristupa chatRoom.getMembers() unutar transakcije,
                    // Hibernate će ih lenjo učitati.
                    // Logovi za admina entiteta
                    if (chatRoom.getAdmin() != null) {
                        logger.debug("  Admin Entity in ChatRoom {}: ID={}, Username='{}', Email='{}'",
                                chatRoom.getId(), chatRoom.getAdmin().getId(), chatRoom.getAdmin().getUsername(), chatRoom.getAdmin().getEmail());
                    } else {
                        logger.debug("  ChatRoom {} has no admin entity.", chatRoom.getId());
                    }

                    // Logovi za članove entiteta
                    // OVO JE MESTO GDE SE SADA DESAVA LAZY LOADING
                    if (chatRoom.getMembers() != null) { // Proveri da li je kolekcija inicijalizovana
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

                    // Sada mapiraj u DTO
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

        // Proveri da li je korisnik član ove sobe (sigurnosna provera)
        Client user = clientRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Client not found with ID: " + userId));

        boolean isMember = chatRoomMemberRepository.findByClientAndChatRoom(user, chatRoom).isPresent();
        if (!isMember) {
            throw new SecurityException("User is not a member of this chat room.");
        }

        return mapChatRoomToDTO(chatRoom);
    }

    /**
     * Pomoćna metoda za interne potrebe servisa za dobijanje ChatRoom entiteta.
     * Nije namenjena za direktno izlaganje putem API-ja.
     * @param chatRoomId ID chat sobe
     * @return ChatRoom entitet
     * @throws EntityNotFoundException ako chat soba nije pronađena
     */
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

        // Provera da li je trenutni korisnik admin grupe
        if (chatRoom.getAdmin() == null || !chatRoom.getAdmin().getId().equals(currentUserId)) {
            throw new SecurityException("Only the group admin can add members.");
        }

        Client newMember = clientRepository.findById(newMemberId)
                .orElseThrow(() -> new EntityNotFoundException("New member not found with ID: " + newMemberId));

        // Proveri da li je već član
        if (chatRoomMemberRepository.findByClientAndChatRoom(newMember, chatRoom).isPresent()) {
            throw new IllegalArgumentException("User is already a member of this chat room.");
        }

        ChatRoomMember member = new ChatRoomMember();
        member.setClient(newMember);
        member.setChatRoom(chatRoom);
        member.setRole(MemberRole.MEMBER);
        chatRoomMemberRepository.save(member);
        chatRoom.getMembers().add(member); // Ažuriraj listu u entitetu
    }

    @Transactional
    public void removeMemberFromGroupChat(Integer chatRoomId, Integer memberToRemoveId, Integer currentUserId) {
        ChatRoom chatRoom = findChatRoomById(chatRoomId);
        if (chatRoom.getType() == ChatRoomType.PRIVATE) {
            throw new IllegalArgumentException("Cannot remove members from a private chat.");
        }

        // Provera da li je trenutni korisnik admin grupe
        if (chatRoom.getAdmin() == null || !chatRoom.getAdmin().getId().equals(currentUserId)) {
            throw new SecurityException("Only the group admin can remove members.");
        }

        Client memberClient = clientRepository.findById(memberToRemoveId)
                .orElseThrow(() -> new EntityNotFoundException("Member to remove not found with ID: " + memberToRemoveId));

        // Ne dozvoli adminu da ukloni samog sebe osim ako nije prebacio admin rolu
        if (chatRoom.getAdmin().getId().equals(memberToRemoveId)) {
            throw new IllegalArgumentException("Admin cannot remove themselves. Transfer admin role first.");
        }

        ChatRoomMember membership = chatRoomMemberRepository.findByClientAndChatRoom(memberClient, chatRoom)
                .orElseThrow(() -> new EntityNotFoundException("Member is not part of this chat room."));

        chatRoom.getMembers().remove(membership); // Ukloni iz liste entiteta
        chatRoomMemberRepository.delete(membership); // Obriši iz baze (zbog orphanRemoval na ChatRoom, možda nije striktno neophodno, ali je sigurnije)
    }


    // --- POMOĆNE METODE ZA MAPIRANJE ENTITETA U DTO ---
    private ChatRoomDto mapChatRoomToDTO(ChatRoom chatRoom) {
        ChatRoomDto dto = new ChatRoomDto();
        dto.setId(chatRoom.getId());
        dto.setName(chatRoom.getName());
        dto.setType(chatRoom.getType());

        // KLJUČNO: Popuni admin DTO
        if (chatRoom.getAdmin() != null) {
            dto.setAdmin(mapClientToDTO(chatRoom.getAdmin()));
            logger.debug("  Mapped admin for ChatRoom ID {}: Client ID {}, Username: {}", chatRoom.getId(), dto.getAdmin().getId(), dto.getAdmin().getUsername());
        } else {
            logger.debug("  ChatRoom ID {} has no admin (likely a private chat).", chatRoom.getId());
        }

        // KLJUČNA IZMENA: Popuni listu članova!
        // Moraš osigurati da chatRoom.getMembers() nije null i da su članovi dohvaćeni
        // (zbog LazyInitializationException, zbog čega smo dodali JOIN FETCH u repozitorijum).
        if (chatRoom.getMembers() != null) {
            List<ClientDto> memberDtos = chatRoom.getMembers().stream()
                    .map(member -> {
                        ClientDto clientDto = mapClientToDTO(member.getClient()); // Mapiraj Client entitet u ClientDto
                        logger.debug("  Mapping member Client ID {} ({}) to DTO for ChatRoom ID {}", clientDto.getId(), clientDto.getUsername(), chatRoom.getId());
                        return clientDto;
                    })
                    .collect(Collectors.toList());
            dto.setMembers(memberDtos); // Postavi popunjenu listu
        } else {
            dto.setMembers(new ArrayList<>()); // Uvek vrati praznu listu ako nema članova
            logger.warn("  ChatRoom ID {} members list is null during DTO mapping. Setting to empty list.", chatRoom.getId());
        }

        // ... (lastMessage ako ga popunjavaš)

        return dto;
    }

    private ClientDto mapClientToDTO(Client client) {
        if (client == null) {
            logger.warn("Attempted to map a null Client entity to ClientDto.");
            return null; // Ili baci izuzetak
        }
        // Proveri da li su ID i username dostupni u Client entitetu
        if (client.getId() == null || client.getUsername() == null) {
            logger.warn("Client entity ID or Username is null for Client ID: {}. Cannot fully map to ClientDto.", client.getId());
            // Možeš vratiti ClientDto sa null vrednostima ili baciti izuzetak
            return new ClientDto(null, null); // Vrati DTO sa null-ovima ako podaci nedostaju
        }
        return new ClientDto(client.getId(), client.getUsername());
    }
}
