package com.socratica.config;

import com.google.genai.Client;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Provides a singleton {@link Client} bean for the Google Gen AI Java SDK using
 * the Gemini Developer API.
 */
@Configuration
@Slf4j
public class GeminiClientConfig {

    @Value("${socratica.gemini.api-key:}")
    private String apiKey;

    @Bean
    public Client geminiClient() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Set GEMINI_API_KEY to enable Gemini text and live requests.");
        }

        log.info("Initializing Gemini Client with API key auth");
        return Client.builder()
                .apiKey(apiKey)
                .build();
    }
}
