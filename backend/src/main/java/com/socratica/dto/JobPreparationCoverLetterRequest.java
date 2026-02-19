package com.socratica.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class JobPreparationCoverLetterRequest {
    @NotBlank(message = "Cover letter text is required")
    @Size(max = 8000, message = "Cover letter text must not exceed 8000 characters")
    private String coverLetterText;

    @Size(max = 4000, message = "Job description must not exceed 4000 characters")
    private String jobDescription;

    @Size(max = 1000, message = "Focus areas must not exceed 1000 characters")
    private String focusAreas;
}
