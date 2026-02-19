package com.socratica.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NotesSummariesRequest {
    @NotBlank(message = "Source text is required")
    @Size(max = 10000, message = "Source text must not exceed 10000 characters")
    private String sourceText;

    @Size(max = 200, message = "Desired length must not exceed 200 characters")
    private String desiredLength;

    @Size(max = 200, message = "Format must not exceed 200 characters")
    private String format;

    @Size(max = 1000, message = "Focus areas must not exceed 1000 characters")
    private String focusAreas;
}
