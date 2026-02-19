package com.socratica.repository;

import com.socratica.entity.AcademicBackground;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AcademicBackgroundRepository extends MongoRepository<AcademicBackground, String> {
    Optional<AcademicBackground> findByUserId(String userId);
}
