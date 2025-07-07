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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatMessageService {
    private static final Logger logger = LoggerFactory.getLogger(ChatMessageService.class);

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ClientRepository clientRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatMessageService(ChatMessageRepository chatMessageRepository,
                              ChatRoomRepository chatRoomRepository,
                              ClientRepository clientRepository,
                              ChatRoomMemberRepository chatRoomMemberRepository,
                              SimpMessagingTemplate messagingTemplate) {
        this.chatMessageRepository = chatMessageRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.clientRepository = clientRepository;
        this.chatRoomMemberRepository = chatRoomMemberRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public ChatMessageDto sendMessage(MessageSendRequestDto request, Integer senderId) {
        ChatRoom chatRoom = chatRoomRepository.findById(request.getChatRoomId())
                .orElseThrow(() -> {
                    return new EntityNotFoundException("Chat soba nije pronađena sa ID: " + request.getChatRoomId());
                });
        Client sender = clientRepository.findById(senderId)
                .orElseThrow(() -> {
                    return new EntityNotFoundException("Pošiljalac (klijent) nije pronađen sa ID: " + senderId);
                });
        boolean isMember = chatRoomMemberRepository.findByClientAndChatRoom(sender, chatRoom).isPresent();
        if (!isMember) {
            throw new SecurityException("Pošiljalac nije član ove chat sobe.");
        }

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setChatRoom(chatRoom);
        chatMessage.setSender(sender);
        chatMessage.setContent(request.getContent());
        chatMessage.setTimestamp(LocalDateTime.now());

        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);
        ChatMessageDto messageDto = mapChatMessageToDTO(savedMessage);
        String destination = "/topic/chat/room/" + chatRoom.getId();
        messagingTemplate.convertAndSend(destination, messageDto);

        return messageDto;
    }

    @Transactional(readOnly = true)
    public List<ChatMessageDto> getChatHistory(Integer chatRoomId, Integer userId, int limit) {

        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> {
                    return new EntityNotFoundException("Chat soba nije pronađena sa ID: " + chatRoomId);
                });

        Client user = clientRepository.findById(userId)
                .orElseThrow(() -> {
                    return new EntityNotFoundException("Korisnik (klijent) nije pronađen sa ID: " + userId);
                });

        ChatRoomMember member = chatRoomMemberRepository.findByClientAndChatRoom(user, chatRoom)
                .orElseThrow(() -> {
                    return new SecurityException("Korisnik nije član ove chat sobe.");
                });

        List<ChatMessage> finalMessages = new ArrayList<>();

        if (chatRoom.getType() == ChatRoomType.GROUP) {
            LocalDateTime joinedAt = member.getJoinedAt();
            if (joinedAt == null) {
                finalMessages = chatMessageRepository.findByChatRoomOrderByTimestampAscWithSender(chatRoom);
            } else {

                List<ChatMessage> messagesBeforeJoined = chatMessageRepository.findTopNByChatRoomIdAndTimestampBefore(
                        chatRoomId, joinedAt, limit);
                Collections.reverse(messagesBeforeJoined);

                List<ChatMessage> messagesAfterJoined = chatMessageRepository.findByChatRoomIdAndTimestampGreaterThanEqualOrderByTimestampAsc(
                        chatRoomId, joinedAt);

                Set<ChatMessage> distinctMessages = new HashSet<>();
                distinctMessages.addAll(messagesBeforeJoined);
                distinctMessages.addAll(messagesAfterJoined);
                finalMessages.addAll(distinctMessages);
                finalMessages.sort(Comparator.comparing(ChatMessage::getTimestamp));
            }
        } else if (chatRoom.getType() == ChatRoomType.PRIVATE) {
            finalMessages = chatMessageRepository.findTopNByChatRoomIdOrderByTimestampDesc(chatRoomId, limit);
            Collections.reverse(finalMessages);
        } else {
            throw new IllegalStateException("Nepoznat tip chat sobe.");
        }

        return finalMessages.stream()
                .map(this::mapChatMessageToDTO)
                .collect(Collectors.toList());
    }

    private ChatMessageDto mapChatMessageToDTO(ChatMessage message) {
        ChatMessageDto dto = new ChatMessageDto();
        dto.setId(message.getId());
        dto.setChatRoomId(message.getChatRoom().getId());
        dto.setContent(message.getContent());
        dto.setTimestamp(message.getTimestamp());

        if (message.getSender() != null) {
            dto.setSender(mapClientToDTO(message.getSender()));
        } else {
            dto.setSender(null);
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
        return new ClientDto(client.getId().longValue(), client.getUsername());
    }
}