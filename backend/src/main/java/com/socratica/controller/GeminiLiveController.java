package com.socratica.controller;

import com.socratica.dto.AudioTranscriptionResponse;
import com.socratica.dto.LiveSessionTokenResponse;
import com.socratica.service.GeminiLiveService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/ai/live")
@RequiredArgsConstructor
public class GeminiLiveController {
    private final GeminiLiveService geminiLiveService;

    @PostMapping("/session-token")
    public ResponseEntity<LiveSessionTokenResponse> createSessionToken() {
        return ResponseEntity.ok(geminiLiveService.createSessionToken());
    }

    @PostMapping("/transcribe")
    public ResponseEntity<AudioTranscriptionResponse> transcribeAudio(@RequestParam("audio") MultipartFile audio) throws Exception {
        String transcript = geminiLiveService.transcribeAudio(audio.getBytes(), audio.getContentType());
        return ResponseEntity.ok(new AudioTranscriptionResponse(transcript));
    }
}
