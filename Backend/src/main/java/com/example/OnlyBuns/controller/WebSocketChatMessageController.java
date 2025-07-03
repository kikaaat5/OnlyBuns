package com.example.OnlyBuns.controller;

import com.example.OnlyBuns.dto.ChatMessageDto;
import com.example.OnlyBuns.dto.MessageSendRequestDto;
import com.example.OnlyBuns.service.ChatMessageService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;
import org.springframework.security.core.Authentication;

@Controller
public class WebSocketChatMessageController {
    private final ChatMessageService chatMessageService;
    private final SimpMessageSendingOperations messagingTemplate;

    public WebSocketChatMessageController(ChatMessageService chatMessageService,
                                          SimpMessageSendingOperations messagingTemplate) {
        this.chatMessageService = chatMessageService;
        this.messagingTemplate = messagingTemplate;
    }
    @MessageMapping("/chat.sendMessage")
    public ChatMessageDto sendMessage(@Payload MessageSendRequestDto request, Authentication authentication) {
        try {
            Integer senderId = Integer.parseInt(authentication.getName());
            ChatMessageDto savedMessage = chatMessageService.sendMessage(request, senderId);
            messagingTemplate.convertAndSend("/topic/chat/room/" + savedMessage.getChatRoomId(), savedMessage);
            return savedMessage;
        } catch (EntityNotFoundException | SecurityException e) {
            System.err.println("Error sending message via WebSocket: " + e.getMessage());
            return null;
        } catch (NumberFormatException e) {
            System.err.println("Invalid user ID format in WebSocket message: " + e.getMessage());
            return null;
        }
    }
}
