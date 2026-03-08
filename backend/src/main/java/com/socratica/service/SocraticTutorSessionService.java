package com.socratica.service;

import com.socratica.dto.SocraticTutorMessageRequest;
import com.socratica.dto.SocraticTutorSessionRequest;
import com.socratica.entity.SocraticTutorMessage;
import com.socratica.entity.SocraticTutorSession;
import com.socratica.repository.SocraticTutorSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SocraticTutorSessionService {
    private final SocraticTutorSessionRepository socraticTutorSessionRepository;

    public SocraticTutorSession saveSession(String userId, SocraticTutorSessionRequest request) {
        SocraticTutorSession session = resolveSession(userId, request.getSessionId());

        session.setUserId(userId);
        session.setTitle(request.getTitle());
        session.setTopic(request.getTopic());
        session.setLearningGoal(request.getLearningGoal());
        session.setTutorMode(request.getTutorMode());
        session.setDemoScript(request.getDemoScript());
        session.setTranscriptEntries(mapMessages(request.getTranscriptEntries()));
        session.setUpdatedAt(LocalDateTime.now());

        if (session.getCreatedAt() == null) {
            session.setCreatedAt(LocalDateTime.now());
        }

        return socraticTutorSessionRepository.save(session);
    }

    public List<SocraticTutorSession> listSessions(String userId) {
        return socraticTutorSessionRepository.findTop20ByUserIdOrderByUpdatedAtDesc(userId);
    }

    public SocraticTutorSession getSession(String userId, String sessionId) {
        return socraticTutorSessionRepository.findByIdAndUserId(sessionId, userId)
            .orElseThrow(() -> new RuntimeException("Tutor session not found"));
    }

    private SocraticTutorSession resolveSession(String userId, String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return SocraticTutorSession.builder().build();
        }

        return socraticTutorSessionRepository.findByIdAndUserId(sessionId, userId)
            .orElseThrow(() -> new RuntimeException("Tutor session not found"));
    }

    private List<SocraticTutorMessage> mapMessages(List<SocraticTutorMessageRequest> transcriptEntries) {
        return transcriptEntries.stream()
            .map(message -> SocraticTutorMessage.builder()
                .speaker(message.getSpeaker())
                .text(message.getText())
                .timestamp(LocalDateTime.now())
                .build())
            .toList();
    }
}
