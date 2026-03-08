package com.socratica.repository;

import com.socratica.entity.SocraticTutorSession;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SocraticTutorSessionRepository extends MongoRepository<SocraticTutorSession, String> {
    List<SocraticTutorSession> findTop20ByUserIdOrderByUpdatedAtDesc(String userId);

    Optional<SocraticTutorSession> findByIdAndUserId(String id, String userId);
}
