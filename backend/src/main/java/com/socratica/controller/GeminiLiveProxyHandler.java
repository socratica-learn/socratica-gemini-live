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
import java.util.Queue;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * WebSocket proxy handler that sits between the browser and the Gemini Live API.
 *
 * Supports two auth modes:
 *  - Vertex AI (when GOOGLE_CLOUD_PROJECT is set): uses Application Default Credentials
 *  - Google AI Studio (when only GEMINI_API_KEY is set): uses API key in the URL
 *
 * Messages arriving before the upstream Gemini socket is ready are queued and
 * flushed once the connection opens, preventing the common race-condition where
 * the setup message is dropped.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class GeminiLiveProxyHandler extends TextWebSocketHandler {

    private static final String VERTEX_AI_WS_BASE_TEMPLATE = "wss://%s-aiplatform.googleapis.com/ws/"
            + "google.cloud.aiplatform.v1.LlmBidiService/BidiGenerateContent";

    private static final String GOOGLE_AI_WS_TEMPLATE = "wss://generativelanguage.googleapis.com/ws/"
            + "google.ai.generativelanguage.v1alpha.GenerativeService.BidiGenerateContent?key=%s";

    @Value("${socratica.gemini.project-id:}")
    private String projectId;

    @Value("${socratica.gemini.api-key:}")
    private String apiKey;

    @Value("${socratica.gemini.location:europe-west4}")
    private String location;

    @Value("${socratica.gemini.live-model:gemini-2.5-flash-native-audio-preview-12-2025}")
    private String liveModel;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final GoogleAccessTokenService googleAccessTokenService;

    /** Maps browser session ID → open Gemini WebSocket. */
    private final Map<String, WebSocket> geminiSockets = new ConcurrentHashMap<>();

    /**
     * Messages that arrive before the Gemini WS is ready are queued here.
     * Once the socket opens, the queue is drained in order.
     */
    private final Map<String, Queue<String>> pendingMessages = new ConcurrentHashMap<>();

    // ─── Browser → Backend ────────────────────────────────────────────────────

    @Override
    public void afterConnectionEstablished(WebSocketSession browserSession) {
        log.debug("Browser WS connected: {}", browserSession.getId());

        boolean useVertexAi = projectId != null && !projectId.isBlank();
        boolean useApiKey = !useVertexAi && apiKey != null && !apiKey.isBlank();

        if (!useVertexAi && !useApiKey) {
            log.error("Neither GOOGLE_CLOUD_PROJECT nor GEMINI_API_KEY is configured — rejecting proxy session {}", browserSession.getId());
            sendErrorAndClose(browserSession, "No Gemini credentials configured on the server. Set GOOGLE_CLOUD_PROJECT or GEMINI_API_KEY.");
            return;
        }

        // Create the pending queue before starting the async connect so messages
        // arriving during the handshake are buffered rather than dropped.
        pendingMessages.put(browserSession.getId(), new ConcurrentLinkedQueue<>());

        String geminiUrl;
        WebSocket.Builder wsBuilder = httpClient.newWebSocketBuilder();

        if (useVertexAi) {
            geminiUrl = String.format(VERTEX_AI_WS_BASE_TEMPLATE, location);
            log.debug("Using Vertex AI WebSocket endpoint: {}", geminiUrl);
            try {
                String accessToken = googleAccessTokenService.getCloudPlatformAccessToken();
                wsBuilder.header("Authorization", "Bearer " + accessToken);
                wsBuilder.header("x-goog-user-project", projectId);
            } catch (RuntimeException e) {
                log.error("Vertex AI auth failed for session {}: {}", browserSession.getId(), e.getMessage());
                pendingMessages.remove(browserSession.getId());
                sendErrorAndClose(browserSession, "Failed to acquire Vertex AI credentials on the server.");
                return;
            }
        } else {
            geminiUrl = String.format(GOOGLE_AI_WS_TEMPLATE, apiKey);
            log.debug("Using Google AI Studio WebSocket endpoint (API key auth)");
        }

        wsBuilder.buildAsync(URI.create(geminiUrl), new GeminiListener(browserSession))
                .thenAccept(geminiWs -> {
                    geminiSockets.put(browserSession.getId(), geminiWs);
                    log.debug("Gemini WS opened for browser session {}", browserSession.getId());
                    // Flush any messages that arrived before the socket was ready.
                    Queue<String> queued = pendingMessages.remove(browserSession.getId());
                    if (queued != null) {
                        String msg;
                        while ((msg = queued.poll()) != null) {
                            log.debug("Flushing queued message to Gemini for session {}", browserSession.getId());
                            geminiWs.sendText(msg, true);
                        }
                    }
                })
                .exceptionally(ex -> {
                    log.error("Failed to open Gemini WS for session {}: {}", browserSession.getId(), ex.getMessage());
                    pendingMessages.remove(browserSession.getId());
                    sendErrorAndClose(browserSession, "Failed to connect to Gemini: " + ex.getMessage());
                    return null;
                });
    }

    @Override
    protected void handleTextMessage(WebSocketSession browserSession, TextMessage message) {
        String payload = injectModelIfSetup(message.getPayload(), browserSession.getId());

        WebSocket geminiWs = geminiSockets.get(browserSession.getId());
        if (geminiWs != null) {
            geminiWs.sendText(payload, true);
            return;
        }

        // Gemini socket not open yet — queue the message.
        Queue<String> queue = pendingMessages.get(browserSession.getId());
        if (queue != null) {
            log.debug("Gemini WS not ready for session {} — queuing message", browserSession.getId());
            queue.add(payload);
        } else {
            log.warn("No Gemini WS and no pending queue for session {} — dropping message", browserSession.getId());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession browserSession, CloseStatus status) {
        log.debug("Browser WS closed: {} ({})", browserSession.getId(), status);
        pendingMessages.remove(browserSession.getId());
        WebSocket geminiWs = geminiSockets.remove(browserSession.getId());
        if (geminiWs != null) {
            geminiWs.sendClose(1000, "Browser disconnected");
        }
    }

    @Override
    public void handleTransportError(WebSocketSession browserSession, Throwable ex) {
        log.error("Transport error for session {}: {}", browserSession.getId(), ex.getMessage());
        pendingMessages.remove(browserSession.getId());
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
            String model = resolveModelResource();
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
            if (!browserSession.isOpen())
                return;
            try {
                browserSession.sendMessage(new TextMessage(text));
            } catch (IOException e) {
                log.error("Failed to forward Gemini message to browser session {}: {}",
                        browserSession.getId(), e.getMessage());
            }
        }
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private String resolveModelResource() {
        // Vertex AI — build the full resource path.
        if (projectId != null && !projectId.isBlank()) {
            if (liveModel.startsWith("projects/")) {
                return liveModel;
            }
            if (liveModel.startsWith("publishers/")) {
                return String.format("projects/%s/locations/%s/%s", projectId, location, liveModel);
            }
            return String.format("projects/%s/locations/%s/publishers/google/models/%s",
                    projectId, location, liveModel);
        }
        // Google AI Studio — bare model name.
        return liveModel;
    }

    private void sendErrorAndClose(WebSocketSession session, String message) {
        if (!session.isOpen())
            return;
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
