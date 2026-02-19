package com.socratica.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SocraticInterruptionRequest {
    @NotBlank(message = "Session context is required")
    @Size(max = 4000, message = "Session context must not exceed 4000 characters")
    private String sessionContext;

    @NotBlank(message = "Last user message is required")
    @Size(max = 2000, message = "Last user message must not exceed 2000 characters")
    private String lastUserMessage;

    @Size(max = 1000, message = "Interruption goal must not exceed 1000 characters")
    private String interruptionGoal;
}
