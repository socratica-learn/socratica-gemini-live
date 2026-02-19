package com.socratica.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "password_reset_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetToken {
    @Id
    private String id;

    @Indexed(unique = true)
    private String token;

    private String email;

    @Builder.Default
    private LocalDateTime expiresAt = LocalDateTime.now().plusHours(1); // Token expires in 1 hour

    @Builder.Default
    private Boolean used = false;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
