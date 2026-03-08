package com.socratica.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LiveSessionTokenResponse {
    private String token;
    private String model;
    private String expiresAt;
    private String newSessionExpiresAt;
}
