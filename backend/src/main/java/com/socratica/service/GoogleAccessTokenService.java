package com.socratica.service;

import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@Slf4j
public class GoogleAccessTokenService {

    private static final List<String> SCOPES = List.of("https://www.googleapis.com/auth/cloud-platform");

    public String getCloudPlatformAccessToken() {
        try {
            GoogleCredentials credentials = GoogleCredentials.getApplicationDefault().createScoped(SCOPES);
            credentials.refreshIfExpired();

            AccessToken accessToken = credentials.getAccessToken();
            if (accessToken == null || accessToken.getTokenValue() == null
                    || accessToken.getTokenValue().isBlank()) {
                throw new IllegalStateException("Google application default credentials returned an empty access token.");
            }

            return accessToken.getTokenValue();
        } catch (IOException e) {
            log.error("Failed to resolve application default credentials for Vertex AI: {}", e.getMessage());
            throw new RuntimeException("Failed to resolve Google application default credentials.", e);
        }
    }
}
