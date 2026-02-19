package com.socratica.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PresentationPrepRequest {
    @NotBlank(message = "Transcript is required")
    @Size(max = 8000, message = "Transcript must not exceed 8000 characters")
    private String transcript;

    @Size(max = 4000, message = "Slide notes must not exceed 4000 characters")
    private String slideNotes;

    @Size(max = 200, message = "Audience must not exceed 200 characters")
    private String audience;

    @Size(max = 1000, message = "Goals must not exceed 1000 characters")
    private String goals;
}
