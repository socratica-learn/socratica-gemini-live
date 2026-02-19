package com.socratica.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class JobPreparationInterviewRequest {
    @NotBlank(message = "Role is required")
    @Size(max = 200, message = "Role must not exceed 200 characters")
    private String role;

    @Size(max = 4000, message = "Job description must not exceed 4000 characters")
    private String jobDescription;

    @Size(max = 8000, message = "Resume text must not exceed 8000 characters")
    private String resumeText;

    @Size(max = 200, message = "Interview type must not exceed 200 characters")
    private String interviewType;

    @Size(max = 200, message = "Difficulty must not exceed 200 characters")
    private String difficulty;
}
