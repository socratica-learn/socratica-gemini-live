package com.socratica.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcademicBackgroundRequest {
    private String fieldOfStudy;
    private String currentRole;
    private String experienceLevel;
    private String backgroundDescription;
}
