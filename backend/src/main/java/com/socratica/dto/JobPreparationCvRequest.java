package com.socratica.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class JobPreparationCvRequest {
    @NotBlank(message = "Resume text is required")
    @Size(max = 8000, message = "Resume text must not exceed 8000 characters")
    private String resumeText;

    @Size(max = 200, message = "Target role must not exceed 200 characters")
    private String targetRole;

    @Size(max = 1000, message = "Focus areas must not exceed 1000 characters")
    private String focusAreas;
}
