package com.socratica.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "academic_backgrounds")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcademicBackground {
    @Id
    private String id;

    private String userId; // Reference to User

    private List<String> fieldOfStudyKeywords; // Extracted keywords only

    private List<String> currentRoleKeywords; // Extracted keywords only

    private String experienceLevel;

    private List<String> backgroundDescriptionKeywords; // Extracted keywords only

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}
