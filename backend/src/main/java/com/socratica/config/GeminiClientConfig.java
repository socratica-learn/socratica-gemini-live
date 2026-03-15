package com.socratica.config;

import com.google.genai.Client;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Provides a singleton {@link Client} bean for the Google Gen AI Java SDK.
 *
 * The API key is resolved from the application config
 * ({@code socratica.gemini.api-key}),
 * which is backed by the server-side {@code GEMINI_API_KEY} environment
 * variable.
 */
@Configuration
@Slf4j
public class GeminiClientConfig {

    @Value("${socratica.gemini.api-key:}")
    private String geminiApiKey;

    @Bean
    public Client geminiClient() {
        String key = resolveKey();
        if (key.isBlank()) {
            log.warn("Gemini API key is not configured — AI features will fail at runtime. "
                    + "Set GEMINI_API_KEY or socratica.gemini.api-key.");
            return Client.builder().apiKey("missing-gemini-api-key").build();
        }
        return Client.builder().apiKey(key).build();
    }

    private String resolveKey() {
        return geminiApiKey == null ? "" : geminiApiKey.trim();
    }
}
