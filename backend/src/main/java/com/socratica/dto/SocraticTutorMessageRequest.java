package com.socratica.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SocraticTutorMessageRequest {
    @NotBlank(message = "Speaker is required")
    @Size(max = 40, message = "Speaker must not exceed 40 characters")
    private String speaker;

    @NotBlank(message = "Message text is required")
    @Size(max = 10000, message = "Message text must not exceed 10000 characters")
    private String text;
}
