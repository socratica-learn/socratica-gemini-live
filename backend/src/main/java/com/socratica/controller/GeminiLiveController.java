package com.socratica.controller;

import com.socratica.dto.LiveSessionTokenResponse;
import com.socratica.service.GeminiLiveService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai/live")
@RequiredArgsConstructor
public class GeminiLiveController {
    private final GeminiLiveService geminiLiveService;

    @PostMapping("/session-token")
    public ResponseEntity<LiveSessionTokenResponse> createSessionToken() {
        return ResponseEntity.ok(geminiLiveService.createSessionToken());
    }
}
