package com.socratica.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.socratica.dto.LiveSessionTokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiLiveService {
    private final ObjectMapper objectMapper;

    @Value("${socratica.gemini.api-key:}")
    private String geminiApiKey;

    @Value("${socratica.gemini.live-model:gemini-2.5-flash-native-audio-preview-12-2025}")
    private String liveModel;

    @Value("${socratica.gemini.model:gemini-2.5-flash}")
    private String geminiModel;

    public LiveSessionTokenResponse createSessionToken() {
        String apiKey = resolveGeminiApiKey();
        if (apiKey.isBlank()) {
            throw new RuntimeException("Gemini API key not configured");
        }

        Instant now = Instant.now();
        String expiresAt = now.plus(30, ChronoUnit.MINUTES).toString();
        String newSessionExpiresAt = now.plus(1, ChronoUnit.MINUTES).toString();

        ObjectNode body = objectMapper.createObjectNode();
        body.put("uses", 1);
        body.put("expireTime", expiresAt);
        body.put("newSessionExpireTime", newSessionExpiresAt);

        String encodedApiKey = URLEncoder.encode(apiKey, StandardCharsets.UTF_8);
        List<String> candidateUrls = List.of(
            "https://generativelanguage.googleapis.com/v1alpha/auth_tokens?key=" + encodedApiKey,
            "https://generativelanguage.googleapis.com/v1alpha/authTokens?key=" + encodedApiKey
        );

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            for (String url : candidateUrls) {
                HttpPost request = new HttpPost(url);
                request.setHeader("Content-Type", "application/json");
                request.setEntity(new StringEntity(body.toString(), ContentType.APPLICATION_JSON));

                try (CloseableHttpResponse response = client.execute(request)) {
                    int status = response.getCode();
                    String responseBody = EntityUtils.toString(response.getEntity());

                    if (status >= 300) {
                        String endpointName = url.contains("authTokens") ? "authTokens" : "auth_tokens";
                        String compactResponseBody = responseBody.replaceAll("\\s+", " ").trim();
                        log.warn("Gemini Live token endpoint {} failed with {}: {}", endpointName, status, compactResponseBody);
                        continue;
                    }

                    JsonNode root = objectMapper.readTree(responseBody);
                    String tokenName = root.path("name").asText("");
                    if (!tokenName.isBlank()) {
                        return new LiveSessionTokenResponse(tokenName, liveModel, expiresAt, newSessionExpiresAt);
                    }

                    log.warn("Gemini Live token response missing token name: {}", responseBody);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create Gemini Live session token", e);
        }

        throw new RuntimeException("Failed to create Gemini Live session token");
    }

    public String transcribeAudio(byte[] audioBytes, String mimeType) {
        String apiKey = resolveGeminiApiKey();
        if (apiKey.isBlank()) {
            throw new RuntimeException("Gemini API key not configured");
        }

        if (audioBytes == null || audioBytes.length == 0) {
            return "";
        }

        String normalizedMimeType = (mimeType == null || mimeType.isBlank()) ? "audio/wav" : mimeType;
        String url = String.format(
            "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s",
            geminiModel,
            URLEncoder.encode(apiKey, StandardCharsets.UTF_8)
        );

        ObjectNode body = objectMapper.createObjectNode();
        ArrayNode contents = body.putArray("contents");
        ObjectNode content = contents.addObject();
        ArrayNode parts = content.putArray("parts");
        parts.addObject().put("text", "Transcribe this audio exactly. Return only the transcript text with no labels or commentary.");
        ObjectNode inlineData = parts.addObject().putObject("inlineData");
        inlineData.put("mimeType", normalizedMimeType);
        inlineData.put("data", java.util.Base64.getEncoder().encodeToString(audioBytes));

        HttpPost request = new HttpPost(url);
        request.setHeader("Content-Type", "application/json");
        request.setEntity(new StringEntity(body.toString(), ContentType.APPLICATION_JSON));

        try (CloseableHttpClient client = HttpClients.createDefault();
             CloseableHttpResponse response = client.execute(request)) {
            int status = response.getCode();
            String responseBody = EntityUtils.toString(response.getEntity());

            if (status >= 300) {
                log.warn("Gemini transcription failed with {}: {}", status, responseBody.replaceAll("\\s+", " ").trim());
                throw new RuntimeException("Gemini transcription failed with status " + status);
            }

            JsonNode root = objectMapper.readTree(responseBody);
            return root.path("candidates")
                .path(0)
                .path("content")
                .path("parts")
                .path(0)
                .path("text")
                .asText("")
                .trim();
        } catch (Exception e) {
            throw new RuntimeException("Failed to transcribe audio", e);
        }
    }

    private String resolveGeminiApiKey() {
        if (geminiApiKey != null && !geminiApiKey.isBlank()) {
            return geminiApiKey;
        }

        String viteGeminiApiKey = System.getenv("VITE_GEMINI_API_KEY");
        return viteGeminiApiKey == null ? "" : viteGeminiApiKey.trim();
    }
}
