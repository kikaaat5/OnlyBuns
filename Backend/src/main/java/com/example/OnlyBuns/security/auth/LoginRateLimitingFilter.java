package com.example.OnlyBuns.security.auth;

import io.github.resilience4j.ratelimiter.RateLimiter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class LoginRateLimitingFilter extends OncePerRequestFilter {

    private final RateLimiter loginRateLimiter;

    public LoginRateLimitingFilter(RateLimiter loginRateLimiter) {
        this.loginRateLimiter = loginRateLimiter;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (request.getRequestURI().equals("/auth/login") && !loginRateLimiter.acquirePermission()) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("Previše pokušaja, molimo pokušajte ponovo kasnije.");
            return;
        }
        filterChain.doFilter(request, response);
    }
}
