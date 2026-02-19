package com.socratica.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonalDetailsRequest {
    private String fullName;
    private String preferredName;
    private String countryTimeZone;
    private String currentRole;
}
