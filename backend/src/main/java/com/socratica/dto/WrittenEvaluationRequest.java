package com.socratica.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class WrittenEvaluationRequest {
    @NotBlank(message = "Topic is required")
    @Size(max = 2000, message = "Topic must not exceed 2000 characters")
    private String topic;

    @NotNull(message = "Question count is required")
    private Integer questionCount;

    @Size(max = 200, message = "Difficulty must not exceed 200 characters")
    private String difficulty;

    @Size(max = 200, message = "Format must not exceed 200 characters")
    private String format;

    @Size(max = 2000, message = "Instructions must not exceed 2000 characters")
    private String instructions;
}
