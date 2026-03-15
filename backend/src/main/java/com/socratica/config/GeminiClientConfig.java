package com.socratica.config;

import com.google.genai.Client;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Provides a singleton {@link Client} bean for the Google Gen AI Java SDK using Vertex AI.
 */
@Configuration
@Slf4j
public class GeminiClientConfig {

    @Value("${socratica.gemini.project-id:}")
    private String projectId;

    @Value("${socratica.gemini.location:europe-west4}")
    private String location;

    @Bean
    public Client geminiClient() {
        if (projectId == null || projectId.isBlank()) {
            throw new IllegalStateException(
                    "GOOGLE_CLOUD_PROJECT must be set when using Vertex AI-backed Gemini services.");
        }

        log.info("Initializing Gemini Client with Vertex AI: project={}, location={}", projectId, location);
        return Client.builder()
                .vertexAI(true)
                .project(projectId)
                .location(location)
                .build();
    }
}
