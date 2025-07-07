package com.example.OnlyBuns.service;

import com.example.OnlyBuns.dto.ChatMessageDto;
import com.example.OnlyBuns.dto.ClientDto;
import com.example.OnlyBuns.dto.MessageSendRequestDto;
import com.example.OnlyBuns.model.*;
import com.example.OnlyBuns.repository.ChatMessageRepository;
import com.example.OnlyBuns.repository.ChatRoomMemberRepository;
import com.example.OnlyBuns.repository.ChatRoomRepository;
import com.example.OnlyBuns.repository.ClientRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate; // <-- KLJUČNI IMPORT

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatMessageService {
    // ISPRAVKA: Logger za ovu klasu
    private static final Logger logger = LoggerFactory.getLogger(ChatMessageService.class);

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ClientRepository clientRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final SimpMessagingTemplate messagingTemplate; // <-- NOVO: Injektuj SimpMessagingTemplate

    // AŽURIRAN KONSTRUKTOR
    public ChatMessageService(ChatMessageRepository chatMessageRepository,
                              ChatRoomRepository chatRoomRepository,
                              ClientRepository clientRepository,
                              ChatRoomMemberRepository chatRoomMemberRepository,
                              SimpMessagingTemplate messagingTemplate) { // <-- DODAT PARAMETAR
        this.chatMessageRepository = chatMessageRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.clientRepository = clientRepository;
        this.chatRoomMemberRepository = chatRoomMemberRepository;
        this.messagingTemplate = messagingTemplate; // <-- INICIJALIZACIJA
    }

    // Metoda za slanje i čuvanje poruka
    @Transactional // Ova metoda mora biti transakciona da bi sačuvala poruku u bazi
    public ChatMessageDto sendMessage(MessageSendRequestDto request, Integer senderId) {
        logger.info("ChatMessageService: Pokušavam da pošaljem poruku. ChatRoomId: {}, SenderId: {}", request.getChatRoomId(), senderId);

        ChatRoom chatRoom = chatRoomRepository.findById(request.getChatRoomId())
                .orElseThrow(() -> {
                    logger.warn("ChatMessageService: Chat soba nije pronađena sa ID: {}", request.getChatRoomId());
                    return new EntityNotFoundException("Chat soba nije pronađena sa ID: " + request.getChatRoomId());
                });
        logger.debug("ChatMessageService: Pronađena Chat soba: {}", chatRoom.getId());

        Client sender = clientRepository.findById(senderId)
                .orElseThrow(() -> {
                    logger.warn("ChatMessageService: Pošiljalac (klijent) nije pronađen sa ID: {}", senderId);
                    return new EntityNotFoundException("Pošiljalac (klijent) nije pronađen sa ID: " + senderId);
                });
        logger.debug("ChatMessageService: Pronađen klijent pošiljaoca: {}", sender.getId());

        // Sigurnosna provera: Proveri da li je pošiljalac član chat sobe
        boolean isMember = chatRoomMemberRepository.findByClientAndChatRoom(sender, chatRoom).isPresent();
        if (!isMember) {
            logger.warn("ChatMessageService: Pošiljalac {} nije član chat sobe {}. Slanje poruke odbijeno.", senderId, chatRoom.getId());
            throw new SecurityException("Pošiljalac nije član ove chat sobe.");
        }

        // Kreiraj ChatMessage entitet
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setChatRoom(chatRoom);
        chatMessage.setSender(sender);
        chatMessage.setContent(request.getContent());
        chatMessage.setTimestamp(LocalDateTime.now()); // Postavi trenutni timestamp

        // Sačuvaj poruku u bazi podataka
        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);
        logger.info("ChatMessageService: Poruka sačuvana u bazi. ID poruke: {}", savedMessage.getId());

        // Mapiraj sačuvanu poruku u DTO
        ChatMessageDto messageDto = mapChatMessageToDTO(savedMessage);
        logger.debug("ChatMessageService: Sačuvana poruka mapirana u DTO. ID poruke: {}", messageDto.getId());

        // <-- KLJUČNA IZMENA: POŠALJI PORUKU NA WEBSOCKET TEMU -->
        String destination = "/topic/chat/room/" + chatRoom.getId();
        logger.info("ChatMessageService: Šaljem poruku na WebSocket temu: {}", destination);
        messagingTemplate.convertAndSend(destination, messageDto);
        logger.info("ChatMessageService: Poruka uspešno poslata na WebSocket.");

        return messageDto;
    }

    @Transactional(readOnly = true)
    public List<ChatMessageDto> getChatHistory(Integer chatRoomId, Integer userId, int limit) {
        /*logger.info("ChatMessageService: Fetching chat history for roomId: {} for user ID: {}", chatRoomId, userId);

        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> {
                    logger.warn("ChatMessageService: Chat Room not found with ID: {}", chatRoomId);
                    return new EntityNotFoundException("Chat Room not found with ID: " + chatRoomId);
                });
        logger.debug("ChatMessageService: Found Chat Room: {}", chatRoom.getId());

        Client user = clientRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.warn("ChatMessageService: Client not found with ID: {}", userId);
                    return new EntityNotFoundException("Client not found with ID: " + userId);
                });
        logger.debug("ChatMessageService: Found Client: {}", user.getId());

        boolean isMember = chatRoomMemberRepository.findByClientAndChatRoom(user, chatRoom).isPresent();
        if (!isMember) {
            logger.warn("ChatMessageService: User {} is not a member of chat room {}. Access denied.", userId, chatRoomId);
            throw new SecurityException("User is not a member of this chat room.");
        }

        List<ChatMessage> messages = chatMessageRepository.findByChatRoomOrderByTimestampAscWithSender(chatRoom);
        logger.info("ChatMessageService: Found {} messages for chat room ID: {}", messages.size(), chatRoomId);

        return messages.stream()
                .map(this::mapChatMessageToDTO)
                .collect(Collectors.toList());*/
        logger.info("ChatMessageService: Dohvatam istoriju chata za sobu ID: {} za korisnika ID: {}, sa limitom: {}", chatRoomId, userId, limit);

        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> {
                    logger.warn("ChatMessageService: Chat soba nije pronađena sa ID: {}", chatRoomId);
                    return new EntityNotFoundException("Chat soba nije pronađena sa ID: " + chatRoomId);
                });

        Client user = clientRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.warn("ChatMessageService: Korisnik (klijent) nije pronađen sa ID: {}", userId);
                    return new EntityNotFoundException("Korisnik (klijent) nije pronađen sa ID: " + userId);
                });

        ChatRoomMember member = chatRoomMemberRepository.findByClientAndChatRoom(user, chatRoom)
                .orElseThrow(() -> {
                    logger.warn("ChatMessageService: Korisnik {} nije član chat sobe {}. Pristup istoriji odbijen.", userId, chatRoomId);
                    return new SecurityException("Korisnik nije član ove chat sobe.");
                });

        List<ChatMessage> finalMessages = new ArrayList<>();

        // Logika za joined_at zavisi od tipa sobe
        if (chatRoom.getType() == ChatRoomType.GROUP) {
            LocalDateTime joinedAt = member.getJoinedAt();
            if (joinedAt == null) {
                // Ovo se ne bi trebalo desiti za grupne chatove ako je logika validna
                // Ali ako se desi, tretiramo kao da je pridružen odavno, tj. dohvatamo sve
                logger.warn("ChatMessageService: 'joinedAt' je NULL za grupni chat sobu {} i člana {}. Dohvatam celu istoriju.", chatRoomId, userId);
                finalMessages = chatMessageRepository.findByChatRoomOrderByTimestampAscWithSender(chatRoom);
            } else {
                // Dohvati 10 NAJNOVIJIH poruka PRE datuma pridruživanja
                // Koristimo joinedAt kao gornju granicu za poruke PRE
                List<ChatMessage> messagesBeforeJoined = chatMessageRepository.findTopNByChatRoomIdAndTimestampBefore(
                        chatRoomId, joinedAt, limit);
                logger.debug("Dohvaćeno {} poruka PRE joinedAt: {} za grupnu sobu: {}", messagesBeforeJoined.size(), joinedAt, chatRoomId);
                Collections.reverse(messagesBeforeJoined); // Obrnuti redosled da budu hronološke

                // Dohvati SVE poruke OD datuma pridruživanja
                List<ChatMessage> messagesAfterJoined = chatMessageRepository.findByChatRoomIdAndTimestampGreaterThanEqualOrderByTimestampAsc(
                        chatRoomId, joinedAt);
                logger.debug("Dohvaćeno {} poruka OD joinedAt: {} za grupnu sobu: {}", messagesAfterJoined.size(), joinedAt, chatRoomId);

                // Kombinuj liste: Prvo poruke pre joinedAt, zatim poruke posle joinedAt
                // Koristimo Set da izbegnemo duplikate.
                Set<ChatMessage> distinctMessages = new HashSet<>();
                distinctMessages.addAll(messagesBeforeJoined);
                distinctMessages.addAll(messagesAfterJoined);
                finalMessages.addAll(distinctMessages);
                finalMessages.sort(Comparator.comparing(ChatMessage::getTimestamp)); // Sortiraj celu listu
            }
        } else if (chatRoom.getType() == ChatRoomType.PRIVATE) {
            // Za privatne četove, uvek dohvati celu istoriju (ili zadnji 'limit' poruka bez obzira na joinedAt)
            logger.info("ChatMessageService: Dohvatam celu istoriju za privatnu sobu ID: {} za korisnika ID: {}", chatRoomId, userId);
            // Možeš koristiti findByChatRoomOrderByTimestampAscWithSender(chatRoom) za celu istoriju,
            // ili novu metodu koja će uzeti samo zadnji 'limit' poruka ako je potrebno
            finalMessages = chatMessageRepository.findTopNByChatRoomIdOrderByTimestampDesc(chatRoomId, limit); // Prilagodi ovo da uzme top N
            Collections.reverse(finalMessages); // Jer želimo uzlazni redosled, a topN je desc
        } else {
            logger.error("ChatMessageService: Nepoznat tip chat sobe: {}", chatRoom.getType());
            throw new IllegalStateException("Nepoznat tip chat sobe.");
        }

        logger.info("ChatMessageService: Ukupno dohvaćeno {} poruka za sobu ID: {} za korisnika ID: {}.", finalMessages.size(), chatRoomId, userId);

        return finalMessages.stream()
                .map(this::mapChatMessageToDTO)
                .collect(Collectors.toList());
    }

    // --- POMOĆNE METODE ZA MAPIRANJE ENTITETA U DTO ---
    private ChatMessageDto mapChatMessageToDTO(ChatMessage message) {
        ChatMessageDto dto = new ChatMessageDto(); // Koristi prazan konstruktor ako @AllArgsConstructor nije u DTO
        dto.setId(message.getId());
        dto.setChatRoomId(message.getChatRoom().getId());
        dto.setContent(message.getContent());
        dto.setTimestamp(message.getTimestamp());

        // Mapiraj pošiljaoca
        if (message.getSender() != null) {
            dto.setSender(mapClientToDTO(message.getSender()));
        } else {
            logger.warn("mapChatMessageToDTO: Message ID {} has a null sender. Setting sender DTO to null.", message.getId());
            dto.setSender(null);
        }
        return dto;
    }

    private ClientDto mapClientToDTO(Client client) {
        if (client == null) {
            logger.warn("mapClientToDTO: Input Client entity is null. Returning null DTO.");
            return null;
        }
        // Proveri da li su ID i username dostupni u Client entitetu
        if (client.getId() == null || client.getUsername() == null) {
            logger.warn("mapClientToDTO: Client entity ID or Username is null for Client ID: {}. Returning DTO with nulls.", client.getId());
            return new ClientDto(null, null);
        }
        // Koristi konstruktor ClientDto(Long id, String username)
        return new ClientDto(client.getId().longValue(), client.getUsername());
    }
}