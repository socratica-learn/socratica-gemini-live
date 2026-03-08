package com.socratica.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    @Value("${socratica.gemini.live-model:gemini-live-2.5-flash-preview}")
    private String liveModel;

    public LiveSessionTokenResponse createSessionToken() {
        if (geminiApiKey == null || geminiApiKey.isBlank()) {
            throw new RuntimeException("Gemini API key not configured");
        }

        Instant now = Instant.now();
        String expiresAt = now.plus(30, ChronoUnit.MINUTES).toString();
        String newSessionExpiresAt = now.plus(1, ChronoUnit.MINUTES).toString();

        ObjectNode body = objectMapper.createObjectNode();
        body.put("uses", 1);
        body.put("expireTime", expiresAt);
        body.put("newSessionExpireTime", newSessionExpiresAt);

        String encodedApiKey = URLEncoder.encode(geminiApiKey, StandardCharsets.UTF_8);
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
                        log.warn("Gemini Live token endpoint failed {} with {}: {}", url, status, responseBody);
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
}
