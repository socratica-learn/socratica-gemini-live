package com.socratica.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@Slf4j
@ConfigurationProperties(prefix = "socratica.oauth")
public class OAuthProperties {
    
    private Google google = new Google();
    private String frontendUrl = "http://localhost:5173";
    
    @PostConstruct
    public void logConfiguration() {
        log.info("OAuth Properties loaded:");
        log.info("  Frontend URL: {}", frontendUrl);
        if (google != null) {
            log.info("  Google Client ID: {} (length: {})", 
                google.clientId != null && !google.clientId.isEmpty() ? google.clientId.substring(0, Math.min(20, google.clientId.length())) + "..." : "EMPTY",
                google.clientId != null ? google.clientId.length() : 0);
            log.info("  Google Redirect URI: {}", google.redirectUri);
        } else {
            log.warn("  Google configuration is NULL!");
        }
    }
    
    @Data
    public static class Google {
        private String clientId = "";
        private String clientSecret = "";
        private String redirectUri = "http://localhost:8080/api/auth/oauth/google/callback";
    }
}
