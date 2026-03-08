package com.socratica.controller;

import com.socratica.dto.SocraticTutorSessionRequest;
import com.socratica.entity.SocraticTutorSession;
import com.socratica.service.SocraticTutorSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ai/live-tutor")
@RequiredArgsConstructor
public class SocraticTutorSessionController {
    private final SocraticTutorSessionService socraticTutorSessionService;

    @GetMapping("/sessions")
    public ResponseEntity<List<SocraticTutorSession>> listSessions(
        @RequestHeader(value = "X-User-Id", required = false) String userId
    ) {
        return ResponseEntity.ok(socraticTutorSessionService.listSessions(requireUserId(userId)));
    }

    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<SocraticTutorSession> getSession(
        @RequestHeader(value = "X-User-Id", required = false) String userId,
        @PathVariable String sessionId
    ) {
        return ResponseEntity.ok(socraticTutorSessionService.getSession(requireUserId(userId), sessionId));
    }

    @PostMapping("/sessions")
    public ResponseEntity<SocraticTutorSession> saveSession(
        @RequestHeader(value = "X-User-Id", required = false) String userId,
        @Valid @RequestBody SocraticTutorSessionRequest request
    ) {
        SocraticTutorSession savedSession =
            socraticTutorSessionService.saveSession(requireUserId(userId), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedSession);
    }

    private String requireUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new RuntimeException("X-User-Id header is required");
        }

        return userId;
    }
}
