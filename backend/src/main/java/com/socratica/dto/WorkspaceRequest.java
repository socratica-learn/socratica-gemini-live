package com.socratica.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkspaceRequest {
    private String theme;
    private String layoutPreference;
    private String topicsStudying;
    private String upcomingDeadlines;
}
