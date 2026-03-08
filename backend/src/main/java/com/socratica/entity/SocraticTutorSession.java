package com.socratica.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "socratic_tutor_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SocraticTutorSession {
    @Id
    private String id;

    private String userId;
    private String title;
    private String topic;
    private String learningGoal;
    private String tutorMode;
    private String demoScript;

    @Builder.Default
    private List<SocraticTutorMessage> transcriptEntries = new ArrayList<>();

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}
