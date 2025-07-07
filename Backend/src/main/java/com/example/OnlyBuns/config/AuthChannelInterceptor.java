package com.example.OnlyBuns.config;

import com.example.OnlyBuns.service.impl.UserServiceImpl;
import com.example.OnlyBuns.util.TokenUtils;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AuthChannelInterceptor implements ChannelInterceptor {

    private final TokenUtils tokenUtils;
    private final UserServiceImpl userService;

    public AuthChannelInterceptor(TokenUtils tokenUtils, UserServiceImpl userService) {
        this.tokenUtils = tokenUtils;
        this.userService = userService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            List<String> authorization = accessor.getNativeHeader("Authorization");
            String authToken = null;

            if (authorization != null && !authorization.isEmpty()) {
                String bearerToken = authorization.get(0);
                if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
                    authToken = bearerToken.substring(7);
                }
            }

            if (authToken != null) {
                String username = tokenUtils.getUsernameFromToken(authToken);

                if (username != null) {
                    UserDetails userDetails = userService.loadUserByUsername(username);
                    if (tokenUtils.validateToken(authToken, userDetails)) {
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());

                        accessor.setUser(authentication);
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            }
        }
        return message;
    }
}