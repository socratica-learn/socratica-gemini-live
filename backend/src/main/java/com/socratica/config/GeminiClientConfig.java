package com.socratica.config;

import com.google.genai.Client;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Provides a singleton {@link Client} bean for the Google Gen AI Java SDK.
 *
 * The API key is resolved from the application config ({@code socratica.gemini.api-key})
 * or the {@code VITE_GEMINI_API_KEY} / {@code GEMINI_API_KEY} environment variables.
 * The key is kept exclusively server-side and never exposed to the frontend.
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
            return new Client(); // SDK will attempt to read GOOGLE_API_KEY from environment
        }
        return Client.builder().apiKey(key).build();
    }

    private String resolveKey() {
        if (geminiApiKey != null && !geminiApiKey.isBlank()) {
            return geminiApiKey;
        }
        String env = System.getenv("GEMINI_API_KEY");
        if (env != null && !env.isBlank()) return env.trim();
        String vite = System.getenv("VITE_GEMINI_API_KEY");
        return vite != null ? vite.trim() : "";
    }
}
