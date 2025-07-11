package com.example.OnlyBuns.config;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Principal; 

@Component
public class AuthChannelInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null) {
            StompCommand command = accessor.getCommand();
            System.out.println("AuthChannelInterceptor: Processing STOMP command: " + command);

            if (StompCommand.CONNECT.equals(command)) {

                Principal principal = accessor.getUser();

                if (principal != null && principal.getName() != null) {
                    System.out.println("AuthChannelInterceptor: User authenticated for STOMP session: " + principal.getName());

                    if (SecurityContextHolder.getContext().getAuthentication() == null ||
                            !SecurityContextHolder.getContext().getAuthentication().getName().equals(principal.getName())) {


                        System.out.println("AuthChannelInterceptor: Setting SecurityContextHolder for " + principal.getName());
                        SecurityContextHolder.getContext().setAuthentication((Authentication) principal); // Cast, assuming Principal is an Authentication
                    }

                } else {
                    System.out.println("AuthChannelInterceptor: No authenticated user (Principal) found in STOMP session for CONNECT command.");

                }
            } else if (StompCommand.SUBSCRIBE.equals(command) || StompCommand.SEND.equals(command)) {
                Principal principal = accessor.getUser();
                if (principal == null || principal.getName() == null) {
                    System.err.println("AuthChannelInterceptor: Unauthorized STOMP command " + command + ": No authenticated user.");

                } else {
                    System.out.println("AuthChannelInterceptor: STOMP command " + command + " from authenticated user: " + principal.getName());

                    if (SecurityContextHolder.getContext().getAuthentication() == null ||
                            !SecurityContextHolder.getContext().getAuthentication().getName().equals(principal.getName())) {
                        SecurityContextHolder.getContext().setAuthentication((Authentication) principal);
                    }
                }
            }
        }
        return message;
    }
}