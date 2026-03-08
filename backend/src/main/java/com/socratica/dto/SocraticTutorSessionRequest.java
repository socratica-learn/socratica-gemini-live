package com.socratica.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SocraticTutorSessionRequest {
    private String sessionId;

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @NotBlank(message = "Topic is required")
    @Size(max = 2000, message = "Topic must not exceed 2000 characters")
    private String topic;

    @Size(max = 2000, message = "Learning goal must not exceed 2000 characters")
    private String learningGoal;

    @Size(max = 100, message = "Tutor mode must not exceed 100 characters")
    private String tutorMode;

    @Size(max = 2000, message = "Demo script must not exceed 2000 characters")
    private String demoScript;

    @Valid
    private List<SocraticTutorMessageRequest> transcriptEntries = new ArrayList<>();
}
