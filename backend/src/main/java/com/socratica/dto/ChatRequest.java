package com.socratica.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChatRequest {
    @NotBlank(message = "Message is required")
    @Size(max = 8000, message = "Message must not exceed 8000 characters")
    private String message;

    @Size(max = 200, message = "Session mode must not exceed 200 characters")
    private String sessionMode;

    @Size(max = 500, message = "Session title must not exceed 500 characters")
    private String sessionTitle;

    @Size(max = 2000, message = "Session topic must not exceed 2000 characters")
    private String sessionTopic;

    @Size(max = 12000, message = "Conversation history must not exceed 12000 characters")
    private String conversationHistory;

    @Size(max = 4000, message = "Document context must not exceed 4000 characters")
    private String documentContext;
}
