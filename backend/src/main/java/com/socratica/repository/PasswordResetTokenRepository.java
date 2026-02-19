package com.socratica.repository;

import com.socratica.entity.PasswordResetToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends MongoRepository<PasswordResetToken, String> {
    Optional<PasswordResetToken> findByTokenAndUsedFalseAndExpiresAtAfter(String token, LocalDateTime now);
    Optional<PasswordResetToken> findByEmailAndUsedFalseAndExpiresAtAfter(String email, LocalDateTime now);
    void deleteByExpiresAtBefore(LocalDateTime now);
}
