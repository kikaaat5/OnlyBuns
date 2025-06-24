package com.example.OnlyBuns.controller;

import com.example.OnlyBuns.service.AnalyticsService;
import com.example.OnlyBuns.dto.PostCommentStatsDto;
import com.example.OnlyBuns.dto.ClientActivityStatsDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
@PreAuthorize("hasRole('ADMIN')")
public class AnalyticsController {
    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/posts-comments-stats")
    public ResponseEntity<PostCommentStatsDto> getPostCommentStatistics() {
        PostCommentStatsDto stats = analyticsService.getPostCommentStatistics();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/client-activity-stats")
    public ResponseEntity<ClientActivityStatsDto> getClientActivityStatistics() {
        ClientActivityStatsDto stats = analyticsService.getClientActivityStatistics();
        return ResponseEntity.ok(stats);
    }
}