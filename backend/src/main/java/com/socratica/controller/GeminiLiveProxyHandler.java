package com.socratica.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.socratica.service.GoogleAccessTokenService;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket proxy handler that sits between the browser and the Vertex AI Live API.
 *
 * The browser connects to /api/ai/live/proxy. This handler opens a corresponding
 * WebSocket to Vertex AI using server-side credentials and forwards messages in both
 * directions transparently.
 *
 * The only transformation applied is injecting the configured model name into the
 * initial "setup" message that the browser sends, so the browser does not need to
 * know which model is in use.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class GeminiLiveProxyHandler extends TextWebSocketHandler {

    private static final String VERTEX_AI_WS_BASE_TEMPLATE =
            "wss://%s-aiplatform.googleapis.com/ws/"
            + "google.cloud.aiplatform.v1.LlmBidiService/BidiGenerateContent";

    @Value("${socratica.gemini.project-id:}")
    private String projectId;

    @Value("${socratica.gemini.api-key:}")
    private String apiKey;

    @Value("${socratica.gemini.location:europe-west4}")
    private String location;

    @Value("${socratica.gemini.live-model:gemini-live-2.5-flash-native-audio}")
    private String liveModel;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final GoogleAccessTokenService googleAccessTokenService;

    /** Maps browser session ID → open Gemini WebSocket. */
    private final Map<String, WebSocket> geminiSockets = new ConcurrentHashMap<>();

    // ─── Browser → Backend ────────────────────────────────────────────────────

    @Override
    public void afterConnectionEstablished(WebSocketSession browserSession) {
        log.debug("Browser WS connected: {}", browserSession.getId());

        if (projectId == null || projectId.isBlank()) {
            log.error("GOOGLE_CLOUD_PROJECT is not configured — rejecting proxy session {}", browserSession.getId());
            if (apiKey != null && !apiKey.isBlank()) {
                sendErrorAndClose(browserSession,
                        "Live voice sessions still require Vertex AI credentials. GOOGLE_API_KEY only enables local backend Gemini calls.");
                return;
            }
            sendErrorAndClose(browserSession, "GOOGLE_CLOUD_PROJECT is not configured on the server.");
            return;
        }

        String geminiUrl = String.format(VERTEX_AI_WS_BASE_TEMPLATE, location);
        WebSocket.Builder wsBuilder = httpClient.newWebSocketBuilder();
        log.debug("Using Vertex AI WebSocket endpoint: {}", geminiUrl);

        try {
            String accessToken = googleAccessTokenService.getCloudPlatformAccessToken();
            wsBuilder.header("Authorization", "Bearer " + accessToken);
            wsBuilder.header("x-goog-user-project", projectId);
        } catch (RuntimeException e) {
            log.error("Vertex AI auth failed for session {}: {}", browserSession.getId(), e.getMessage());
            sendErrorAndClose(browserSession, "Failed to acquire Vertex AI credentials on the server.");
            return;
        }

        wsBuilder.buildAsync(URI.create(geminiUrl), new GeminiListener(browserSession))
                .thenAccept(geminiWs -> {
                    geminiSockets.put(browserSession.getId(), geminiWs);
                    log.debug("Gemini WS opened for browser session {}", browserSession.getId());
                })
                .exceptionally(ex -> {
                    log.error("Failed to open Gemini WS for session {}: {}", browserSession.getId(), ex.getMessage());
                    sendErrorAndClose(browserSession, "Failed to connect to Gemini: " + ex.getMessage());
                    return null;
                });
    }

    @Override
    protected void handleTextMessage(WebSocketSession browserSession, TextMessage message) {
        WebSocket geminiWs = geminiSockets.get(browserSession.getId());
        if (geminiWs == null) {
            log.warn("No Gemini WS for session {} — dropping message", browserSession.getId());
            return;
        }

        String payload = injectModelIfSetup(message.getPayload(), browserSession.getId());
        geminiWs.sendText(payload, true);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession browserSession, CloseStatus status) {
        log.debug("Browser WS closed: {} ({})", browserSession.getId(), status);
        WebSocket geminiWs = geminiSockets.remove(browserSession.getId());
        if (geminiWs != null) {
            geminiWs.sendClose(1000, "Browser disconnected");
        }
    }

    @Override
    public void handleTransportError(WebSocketSession browserSession, Throwable ex) {
        log.error("Transport error for session {}: {}", browserSession.getId(), ex.getMessage());
        WebSocket geminiWs = geminiSockets.remove(browserSession.getId());
        if (geminiWs != null) {
            geminiWs.sendClose(1011, "Transport error");
        }
    }

    // ─── Setup message transformation ─────────────────────────────────────────

    /**
     * If the payload is a Gemini "setup" message, inject the server-configured
     * model name so the browser never needs to know which model is in use.
     */
    private String injectModelIfSetup(String payload, String sessionId) {
        try {
            JsonNode root = objectMapper.readTree(payload);
            if (!root.has("setup")) {
                return payload;
            }
            ObjectNode setup = (ObjectNode) root.get("setup");
            String model = resolveVertexModelResource();
            setup.put("model", model);
            String modified = objectMapper.writeValueAsString(root);
            log.debug("Setup message for session {} — injected model '{}'", sessionId, model);
            return modified;
        } catch (Exception e) {
            log.warn("Could not parse setup message for session {}: {}", sessionId, e.getMessage());
            return payload;
        }
    }

    // ─── Gemini → Backend ─────────────────────────────────────────────────────

    private class GeminiListener implements WebSocket.Listener {

        private final WebSocketSession browserSession;
        /** Buffer for fragmented Gemini messages (rare but possible). */
        private final StringBuilder buffer = new StringBuilder();

        GeminiListener(WebSocketSession browserSession) {
            this.browserSession = browserSession;
        }

        @Override
        public void onOpen(WebSocket webSocket) {
            webSocket.request(1);
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            buffer.append(data);
            webSocket.request(1);
            if (last) {
                String text = buffer.toString();
                buffer.setLength(0);
                forwardToBrowser(text);
            }
            return null;
        }

        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            log.debug("Gemini WS closed for session {}: {} '{}'",
                    browserSession.getId(), statusCode, reason);
            geminiSockets.remove(browserSession.getId());
            if (browserSession.isOpen()) {
                try {
                    int code = (statusCode >= 1000 && statusCode <= 4999) ? statusCode : 1000;
                    browserSession.close(new CloseStatus(code,
                            reason != null && !reason.isBlank() ? reason : "Gemini session ended"));
                } catch (IOException e) {
                    log.error("Failed to close browser session {}: {}", browserSession.getId(), e.getMessage());
                }
            }
            return null;
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            log.error("Gemini WS error for session {}: {}", browserSession.getId(), error.getMessage());
            geminiSockets.remove(browserSession.getId());
            sendErrorAndClose(browserSession, "Gemini connection error: " + error.getMessage());
        }

        private void forwardToBrowser(String text) {
            if (!browserSession.isOpen()) return;
            try {
                browserSession.sendMessage(new TextMessage(text));
            } catch (IOException e) {
                log.error("Failed to forward Gemini message to browser session {}: {}",
                        browserSession.getId(), e.getMessage());
            }
        }
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private String resolveVertexModelResource() {
        if (liveModel.startsWith("projects/")) {
            return liveModel;
        }
        if (liveModel.startsWith("publishers/")) {
            return String.format("projects/%s/locations/%s/%s", projectId, location, liveModel);
        }
        if (projectId == null || projectId.isBlank()) {
            throw new IllegalStateException("GOOGLE_CLOUD_PROJECT must be set for Vertex AI live sessions.");
        }
        return String.format("projects/%s/locations/%s/publishers/google/models/%s",
                projectId, location, liveModel);
    }

    private void sendErrorAndClose(WebSocketSession session, String message) {
        if (!session.isOpen()) return;
        try {
            String errorJson = objectMapper.writeValueAsString(
                    Map.of("error", Map.of("message", message)));
            session.sendMessage(new TextMessage(errorJson));
            session.close(CloseStatus.SERVER_ERROR);
        } catch (IOException e) {
            log.error("Failed to send error to session {}: {}", session.getId(), e.getMessage());
        }
    }
}
