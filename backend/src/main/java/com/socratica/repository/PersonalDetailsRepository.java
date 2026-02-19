package com.socratica.repository;

import com.socratica.entity.PersonalDetails;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PersonalDetailsRepository extends MongoRepository<PersonalDetails, String> {
    Optional<PersonalDetails> findByUserId(String userId);
}
