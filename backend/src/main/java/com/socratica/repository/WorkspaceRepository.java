package com.socratica.repository;

import com.socratica.entity.Workspace;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkspaceRepository extends MongoRepository<Workspace, String> {
    Optional<Workspace> findByUserId(String userId);
}
