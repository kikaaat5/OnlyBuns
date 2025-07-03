package com.example.OnlyBuns.service;

import com.example.OnlyBuns.dto.ChatMessageDto;
import com.example.OnlyBuns.dto.ClientDto;
import com.example.OnlyBuns.dto.MessageSendRequestDto;
import com.example.OnlyBuns.model.ChatMessage;
import com.example.OnlyBuns.model.ChatRoom;
import com.example.OnlyBuns.model.Client;
import com.example.OnlyBuns.repository.ChatMessageRepository;
import com.example.OnlyBuns.repository.ChatRoomMemberRepository;
import com.example.OnlyBuns.repository.ChatRoomRepository;
import com.example.OnlyBuns.repository.ClientRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Collections;
import java.util.Comparator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatMessageService {
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ClientRepository clientRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;

    public ChatMessageService(ChatMessageRepository chatMessageRepository,
                              ChatRoomRepository chatRoomRepository,
                              ClientRepository clientRepository,
                              ChatRoomMemberRepository chatRoomMemberRepository) {
        this.chatMessageRepository = chatMessageRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.clientRepository = clientRepository;
        this.chatRoomMemberRepository = chatRoomMemberRepository;
    }

    @Transactional
    public ChatMessageDto sendMessage(MessageSendRequestDto request, Integer senderId) {
        Client sender = clientRepository.findById(senderId)
                .orElseThrow(() -> new EntityNotFoundException("Sender client not found with ID: " + senderId));
        ChatRoom chatRoom = chatRoomRepository.findById(request.getChatRoomId())
                .orElseThrow(() -> new EntityNotFoundException("Chat Room not found with ID: " + request.getChatRoomId()));

        // Proveri da li je pošiljalac član te sobe
        boolean isMember = chatRoomMemberRepository.findByClientAndChatRoom(sender, chatRoom).isPresent();
        if (!isMember) {
            throw new SecurityException("Sender is not a member of this chat room.");
        }

        ChatMessage message = new ChatMessage();
        message.setSender(sender);
        message.setChatRoom(chatRoom);
        message.setContent(request.getContent());
        message.setTimestamp(LocalDateTime.now()); // Postavi trenutno vreme

        message = chatMessageRepository.save(message);

        return mapChatMessageToDTO(message);
    }

    @Transactional(readOnly = true)
    public List<ChatMessageDto> getChatHistory(Integer chatRoomId, Integer userId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new EntityNotFoundException("Chat Room not found with ID: " + chatRoomId));

        // Proveri da li je korisnik član ove sobe pre nego što vratiš istoriju (sigurnost)
        Client user = clientRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Client not found with ID: " + userId));

        boolean isMember = chatRoomMemberRepository.findByClientAndChatRoom(user, chatRoom).isPresent();
        if (!isMember) {
            throw new SecurityException("User is not a member of this chat room and cannot view history.");
        }

        // Dohvati poruke sortirane po vremenu
        List<ChatMessage> messages = chatMessageRepository.findByChatRoomOrderByTimestampAsc(chatRoom);

        return messages.stream()
                .map(this::mapChatMessageToDTO)
                .collect(Collectors.toList());
    }

    // --- POMOĆNE METODE ZA MAPIRANJE ENTITETA U DTO ---
    private ChatMessageDto mapChatMessageToDTO(ChatMessage message) {
        return new ChatMessageDto(
                message.getId(),
                mapClientToDTO(message.getSender()),
                message.getChatRoom().getId(),
                message.getContent(),
                message.getTimestamp()
        );
    }

    private ClientDto mapClientToDTO(Client client) {
        return new ClientDto(client.getId(), client.getUsername());
    }
}
