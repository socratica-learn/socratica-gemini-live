package com.socratica.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TeachingAdviceRequest {
    @NotBlank(message = "Topic is required")
    @Size(max = 2000, message = "Topic must not exceed 2000 characters")
    private String topic;

    @Size(max = 200, message = "Audience must not exceed 200 characters")
    private String audience;

    @Size(max = 1000, message = "Constraints must not exceed 1000 characters")
    private String constraints;

    @Size(max = 200, message = "Tone must not exceed 200 characters")
    private String tone;
}
