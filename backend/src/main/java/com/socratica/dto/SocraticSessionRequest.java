package com.socratica.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SocraticSessionRequest {
    @NotBlank(message = "Topic is required")
    @Size(max = 2000, message = "Topic must not exceed 2000 characters")
    private String topic;

    @Size(max = 200, message = "Student level must not exceed 200 characters")
    private String studentLevel;

    @Size(max = 2000, message = "Goals must not exceed 2000 characters")
    private String goals;

    @Size(max = 200, message = "Conversation style must not exceed 200 characters")
    private String conversationStyle;

    @Size(max = 4000, message = "Context must not exceed 4000 characters")
    private String context;
}
