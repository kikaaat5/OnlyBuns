package com.example.OnlyBuns.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class CommentRateLimiterService {
    // userId → lista vremena komentara
    private final Map<Integer, List<LocalDateTime>> userCommentTimestamps = new HashMap<>();

    private static final int LIMIT = 60; // 60 komentara
    private static final int WINDOW_HOURS = 1; // u 1 satu

    public synchronized boolean canComment(int userId) {
        LocalDateTime now = LocalDateTime.now();
        List<LocalDateTime> timestamps = userCommentTimestamps.getOrDefault(userId, new ArrayList<>());

        // Ukloni one starije od 1 sata
        timestamps.removeIf(time -> time.isBefore(now.minusHours(WINDOW_HOURS)));

        if (timestamps.size() >= LIMIT) {
            return false;
        }

        // Dodaj trenutni komentar
        timestamps.add(now);
        userCommentTimestamps.put(userId, timestamps);
        return true;
    }
}

