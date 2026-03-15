package com.socratica.service;

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiLiveService {

    private final Client geminiClient;

    @Value("${socratica.gemini.model:gemini-2.5-flash}")
    private String geminiModel;

    /**
     * Transcribes audio bytes using the Gemini SDK with multimodal content.
     */
    public String transcribeAudio(byte[] audioBytes, String mimeType) {
        if (audioBytes == null || audioBytes.length == 0) {
            return "";
        }

        String normalizedMimeType = (mimeType == null || mimeType.isBlank()) ? "audio/wav" : mimeType;

        try {
            Content content = Content.fromParts(
                Part.fromText("Transcribe this audio exactly. Return only the transcript text with no labels or commentary."),
                Part.fromBytes(audioBytes, normalizedMimeType)
            );
            GenerateContentResponse response = geminiClient.models.generateContent(geminiModel, content, null);
            String text = response.text();
            return text != null ? text.trim() : "";
        } catch (Exception e) {
            log.error("Gemini transcription failed: {}", e.getMessage());
            throw new RuntimeException("Failed to transcribe audio", e);
        }
    }
}
