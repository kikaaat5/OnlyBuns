package com.example.OnlyBuns.security.auth;

import com.example.OnlyBuns.service.UserService;
import com.example.OnlyBuns.util.TokenUtils;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class WebSocketJwtFilter extends OncePerRequestFilter {

    private TokenUtils tokenUtils;
    private UserService userService;

    public WebSocketJwtFilter(TokenUtils tokenUtils, UserService userService) {
        this.tokenUtils = tokenUtils;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestUri = request.getRequestURI();

        if (requestUri.startsWith("/ws")) {
            String token = request.getParameter("token");

            if (token != null) {

                System.out.println("WebSocketJwtFilter: Token found in URI query parameter for WebSocket: " + token);
                String username = null;
                try {
                    username = tokenUtils.getUsernameFromToken(token);
                } catch (IllegalArgumentException e) {
                    System.out.println("WebSocketJwtFilter: Unable to get JWT username: " + e.getMessage());
                } catch (ExpiredJwtException e) {
                    System.out.println("WebSocketJwtFilter: JWT token has expired: " + e.getMessage());
                }

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = this.userService.findByEmail(username); // Koristi loadUserByUsername

                    if (tokenUtils.validateToken(token, userDetails)) {
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        System.out.println("WebSocketJwtFilter: User authenticated for WebSocket: " + username + " (via URL token)");
                    } else {
                        System.out.println("WebSocketJwtFilter: Token validation failed for user: " + username);
                    }
                }
            } else {
                System.out.println("WebSocketJwtFilter: No token found in URI query parameter for WebSocket.");
            }
        }

        filterChain.doFilter(request, response);
    }
}
