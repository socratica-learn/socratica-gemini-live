package com.socratica.controller;

import com.socratica.dto.AcademicBackgroundRequest;
import com.socratica.dto.PersonalDetailsRequest;
import com.socratica.dto.WorkspaceRequest;
import com.socratica.entity.AcademicBackground;
import com.socratica.entity.PersonalDetails;
import com.socratica.entity.Workspace;
import com.socratica.service.PersonalizeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/personalize")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PersonalizeController {

    private final PersonalizeService personalizeService;

    @PostMapping("/personal-details")
    public ResponseEntity<PersonalDetails> savePersonalDetails(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestBody PersonalDetailsRequest request) {
        try {
            // For now, accept userId from header or request body
            // TODO: Extract from JWT token in the future
            if (userId == null || userId.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
            PersonalDetails saved = personalizeService.savePersonalDetails(userId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/academic-background")
    public ResponseEntity<AcademicBackground> saveAcademicBackground(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestBody AcademicBackgroundRequest request) {
        try {
            if (userId == null || userId.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
            AcademicBackground saved = personalizeService.saveAcademicBackground(userId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/workspace")
    public ResponseEntity<Workspace> saveWorkspace(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestBody WorkspaceRequest request) {
        try {
            if (userId == null || userId.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
            Workspace saved = personalizeService.saveWorkspace(userId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
