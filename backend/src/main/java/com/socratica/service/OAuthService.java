package com.socratica.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.socratica.config.OAuthProperties;
import com.socratica.dto.AuthResponse;
import com.socratica.entity.User;
import com.socratica.repository.UserRepository;
import com.socratica.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class OAuthService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final OAuthProperties oAuthProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Get Google OAuth authorization URL
     */
    public String getGoogleAuthUrl(String frontendOrigin) {
        if (oAuthProperties == null || oAuthProperties.getGoogle() == null) {
            log.error("OAuthProperties or Google configuration is null");
            throw new IllegalStateException("OAuth configuration is not properly initialized. Please check application.yml");
        }
        
        String googleClientId = oAuthProperties.getGoogle().getClientId();
        String googleRedirectUri = oAuthProperties.getGoogle().getRedirectUri();
        
        log.debug("Google OAuth config - ClientId: '{}', RedirectUri: '{}'", 
            googleClientId != null ? googleClientId.substring(0, Math.min(20, googleClientId.length())) + "..." : "null",
            googleRedirectUri);
        
        if (googleClientId == null || googleClientId.isEmpty()) {
            log.error("Google OAuth client ID is not configured. Current value: '{}'", googleClientId);
            throw new IllegalStateException("Google OAuth client ID is not configured. Please set socratica.oauth.google.client-id in application.yml");
        }
        if (googleRedirectUri == null || googleRedirectUri.isEmpty()) {
            log.error("Google OAuth redirect URI is not configured. Current value: '{}'", googleRedirectUri);
            throw new IllegalStateException("Google OAuth redirect URI is not configured. Please set socratica.oauth.google.redirect-uri in application.yml");
        }
        
        try {
            String encodedClientId = URLEncoder.encode(googleClientId, StandardCharsets.UTF_8);
            String scope = URLEncoder.encode("openid email profile", StandardCharsets.UTF_8);
            String redirectUri = URLEncoder.encode(googleRedirectUri, StandardCharsets.UTF_8);
            String stateParam = buildStateParam(frontendOrigin);
            
            String authUrl = String.format(
                "https://accounts.google.com/o/oauth2/v2/auth?client_id=%s&redirect_uri=%s&response_type=code&scope=%s&access_type=online%s",
                encodedClientId,
                redirectUri,
                scope,
                stateParam
            );
            
            log.debug("Generated Google OAuth URL successfully");
            return authUrl;
        } catch (Exception e) {
            log.error("Error generating Google OAuth URL: {}", e.getMessage(), e);
            throw new IllegalStateException("Failed to generate Google OAuth URL: " + e.getMessage(), e);
        }
    }

    /**
     * Handle Google OAuth callback
     */
    public AuthResponse handleGoogleCallback(String code) throws IOException {
        // Exchange code for access token
        String accessToken = exchangeGoogleCodeForToken(code);
        
        // Get user info from Google
        GoogleUserInfo userInfo = getGoogleUserInfo(accessToken);
        
        // Create or update user
        User user = createOrUpdateUser(
            userInfo.getEmail(),
            userInfo.getGivenName(),
            userInfo.getFamilyName()
        );
        
        // Generate JWT token
        String token = jwtService.generateToken(user.getEmail());
        
        return AuthResponse.builder()
            .token(token)
            .user(AuthResponse.UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .surname(user.getSurname())
                .build())
            .build();
    }

    private String exchangeGoogleCodeForToken(String code) throws IOException {
        if (oAuthProperties == null || oAuthProperties.getGoogle() == null) {
            throw new IllegalStateException("OAuth configuration is not properly initialized");
        }
        
        String googleClientId = oAuthProperties.getGoogle().getClientId();
        String googleClientSecret = oAuthProperties.getGoogle().getClientSecret();
        String googleRedirectUri = oAuthProperties.getGoogle().getRedirectUri();
        
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost request = new HttpPost("https://oauth2.googleapis.com/token");
            request.setHeader("Content-Type", "application/x-www-form-urlencoded");
            
            String params = String.format(
                "code=%s&client_id=%s&client_secret=%s&redirect_uri=%s&grant_type=authorization_code",
                URLEncoder.encode(code, StandardCharsets.UTF_8),
                URLEncoder.encode(googleClientId, StandardCharsets.UTF_8),
                URLEncoder.encode(googleClientSecret, StandardCharsets.UTF_8),
                URLEncoder.encode(googleRedirectUri, StandardCharsets.UTF_8)
            );
            
            request.setEntity(new StringEntity(params));
            
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                JsonNode jsonNode = objectMapper.readTree(response.getEntity().getContent());
                return jsonNode.get("access_token").asText();
            }
        }
    }

    private GoogleUserInfo getGoogleUserInfo(String accessToken) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            org.apache.hc.client5.http.classic.methods.HttpGet request = 
                new org.apache.hc.client5.http.classic.methods.HttpGet("https://www.googleapis.com/oauth2/v2/userinfo");
            request.setHeader("Authorization", "Bearer " + accessToken);
            
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                JsonNode jsonNode = objectMapper.readTree(response.getEntity().getContent());
                return new GoogleUserInfo(
                    jsonNode.get("email").asText(),
                    jsonNode.has("given_name") ? jsonNode.get("given_name").asText() : "",
                    jsonNode.has("family_name") ? jsonNode.get("family_name").asText() : ""
                );
            }
        }
    }

    private User createOrUpdateUser(String email, String name, String surname) {
        User user = userRepository.findByEmail(email).orElse(null);
        LocalDateTime now = LocalDateTime.now();
        
        if (user == null) {
            // Create new user
            user = User.builder()
                .email(email)
                .name(name != null ? name : "")
                .surname(surname != null ? surname : "")
                .password("") // OAuth users don't have passwords
                .active(true)
                .createdAt(now)
                .updatedAt(now)
                .build();
        } else {
            // Update existing user
            if (name != null && !name.isEmpty()) user.setName(name);
            if (surname != null && !surname.isEmpty()) user.setSurname(surname);
            user.setUpdatedAt(now);
        }
        
        return userRepository.save(user);
    }

    public String getFrontendUrl() {
        return oAuthProperties.getFrontendUrl();
    }

    public String resolveFrontendUrl(String encodedState) {
        String configuredFrontendUrl = oAuthProperties.getFrontendUrl();
        String frontendOrigin = decodeState(encodedState);

        if (frontendOrigin == null || frontendOrigin.isBlank()) {
            return configuredFrontendUrl;
        }

        if (isAllowedFrontendOrigin(frontendOrigin, configuredFrontendUrl)) {
            return frontendOrigin;
        }

        log.warn("Ignoring untrusted frontend origin '{}' and falling back to '{}'", frontendOrigin, configuredFrontendUrl);
        return configuredFrontendUrl;
    }

    private String buildStateParam(String frontendOrigin) {
        String normalizedOrigin = normalizeFrontendOrigin(frontendOrigin);
        if (normalizedOrigin == null) {
            return "";
        }

        String encodedState = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(normalizedOrigin.getBytes(StandardCharsets.UTF_8));
        return "&state=" + URLEncoder.encode(encodedState, StandardCharsets.UTF_8);
    }

    private String decodeState(String encodedState) {
        if (encodedState == null || encodedState.isBlank()) {
            return null;
        }

        try {
            byte[] decodedBytes = Base64.getUrlDecoder().decode(encodedState);
            return normalizeFrontendOrigin(new String(decodedBytes, StandardCharsets.UTF_8));
        } catch (IllegalArgumentException e) {
            log.warn("Failed to decode OAuth state");
            return null;
        }
    }

    private boolean isAllowedFrontendOrigin(String requestedOrigin, String configuredFrontendUrl) {
        String normalizedRequestedOrigin = normalizeFrontendOrigin(requestedOrigin);
        String normalizedConfiguredOrigin = normalizeFrontendOrigin(configuredFrontendUrl);

        if (normalizedRequestedOrigin == null || normalizedConfiguredOrigin == null) {
            return false;
        }

        if (normalizedRequestedOrigin.equals(normalizedConfiguredOrigin)) {
            return true;
        }

        URI configuredUri = URI.create(normalizedConfiguredOrigin);
        boolean configuredIsLocalhost = configuredUri.getHost() != null &&
                ("localhost".equalsIgnoreCase(configuredUri.getHost()) || "127.0.0.1".equals(configuredUri.getHost()));

        if (!configuredIsLocalhost) {
            return false;
        }

        URI requestedUri = URI.create(normalizedRequestedOrigin);
        return "https".equalsIgnoreCase(requestedUri.getScheme()) ||
                "localhost".equalsIgnoreCase(requestedUri.getHost()) ||
                "127.0.0.1".equals(requestedUri.getHost());
    }

    private String normalizeFrontendOrigin(String frontendOrigin) {
        if (frontendOrigin == null || frontendOrigin.isBlank()) {
            return null;
        }

        try {
            URI uri = URI.create(frontendOrigin.trim());
            String scheme = uri.getScheme();
            String host = uri.getHost();

            if (scheme == null || host == null) {
                return null;
            }

            boolean isHttp = "http".equalsIgnoreCase(scheme);
            boolean isHttps = "https".equalsIgnoreCase(scheme);
            if (!isHttp && !isHttps) {
                return null;
            }

            if (uri.getUserInfo() != null || uri.getPath() != null && !uri.getPath().isBlank() && !"/".equals(uri.getPath()) ||
                    uri.getQuery() != null || uri.getFragment() != null) {
                return null;
            }

            return new URI(
                    scheme.toLowerCase(),
                    null,
                    host.toLowerCase(),
                    uri.getPort(),
                    null,
                    null,
                    null
            ).toString();
        } catch (Exception e) {
            return null;
        }
    }

    // Inner classes for user info
    private static class GoogleUserInfo {
        private final String email;
        private final String givenName;
        private final String familyName;

        public GoogleUserInfo(String email, String givenName, String familyName) {
            this.email = email;
            this.givenName = givenName;
            this.familyName = familyName;
        }

        public String getEmail() { return email; }
        public String getGivenName() { return givenName; }
        public String getFamilyName() { return familyName; }
    }

}
