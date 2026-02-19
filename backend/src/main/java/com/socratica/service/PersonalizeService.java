package com.socratica.service;

import com.socratica.dto.AcademicBackgroundRequest;
import com.socratica.dto.PersonalDetailsRequest;
import com.socratica.dto.WorkspaceRequest;
import com.socratica.entity.AcademicBackground;
import com.socratica.entity.PersonalDetails;
import com.socratica.entity.Workspace;
import com.socratica.repository.AcademicBackgroundRepository;
import com.socratica.repository.PersonalDetailsRepository;
import com.socratica.repository.WorkspaceRepository;
import com.socratica.util.KeywordExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PersonalizeService {
    private final PersonalDetailsRepository personalDetailsRepository;
    private final AcademicBackgroundRepository academicBackgroundRepository;
    private final WorkspaceRepository workspaceRepository;

    public PersonalDetails savePersonalDetails(String userId, PersonalDetailsRequest request) {
        // Check if personal details already exist for this user
        PersonalDetails existing = personalDetailsRepository.findByUserId(userId).orElse(null);
        
        // Extract keywords from text fields
        List<String> countryTimeZoneKeywords = KeywordExtractor.extractKeywords(request.getCountryTimeZone());
        List<String> currentRoleKeywords = KeywordExtractor.extractKeywords(request.getCurrentRole());
        
        if (existing != null) {
            // Update existing - only store keywords
            existing.setFullName(request.getFullName());
            existing.setPreferredName(request.getPreferredName());
            existing.setCountryTimeZoneKeywords(countryTimeZoneKeywords);
            existing.setCurrentRoleKeywords(currentRoleKeywords);
            existing.setUpdatedAt(LocalDateTime.now());
            return personalDetailsRepository.save(existing);
        } else {
            // Create new - only store keywords
            PersonalDetails personalDetails = PersonalDetails.builder()
                    .userId(userId)
                    .fullName(request.getFullName())
                    .preferredName(request.getPreferredName())
                    .countryTimeZoneKeywords(countryTimeZoneKeywords)
                    .currentRoleKeywords(currentRoleKeywords)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            return personalDetailsRepository.save(personalDetails);
        }
    }

    public AcademicBackground saveAcademicBackground(String userId, AcademicBackgroundRequest request) {
        // Check if academic background already exists for this user
        AcademicBackground existing = academicBackgroundRepository.findByUserId(userId).orElse(null);
        
        // Extract keywords from text fields
        List<String> fieldOfStudyKeywords = KeywordExtractor.extractKeywords(request.getFieldOfStudy());
        List<String> currentRoleKeywords = KeywordExtractor.extractKeywords(request.getCurrentRole());
        List<String> backgroundDescriptionKeywords = KeywordExtractor.extractKeywords(request.getBackgroundDescription());
        
        if (existing != null) {
            // Update existing - only store keywords
            existing.setFieldOfStudyKeywords(fieldOfStudyKeywords);
            existing.setCurrentRoleKeywords(currentRoleKeywords);
            existing.setExperienceLevel(request.getExperienceLevel());
            existing.setBackgroundDescriptionKeywords(backgroundDescriptionKeywords);
            existing.setUpdatedAt(LocalDateTime.now());
            return academicBackgroundRepository.save(existing);
        } else {
            // Create new - only store keywords
            AcademicBackground academicBackground = AcademicBackground.builder()
                    .userId(userId)
                    .fieldOfStudyKeywords(fieldOfStudyKeywords)
                    .currentRoleKeywords(currentRoleKeywords)
                    .experienceLevel(request.getExperienceLevel())
                    .backgroundDescriptionKeywords(backgroundDescriptionKeywords)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            return academicBackgroundRepository.save(academicBackground);
        }
    }

    public Workspace saveWorkspace(String userId, WorkspaceRequest request) {
        // Check if workspace already exists for this user
        Workspace existing = workspaceRepository.findByUserId(userId).orElse(null);
        
        // Extract keywords from text fields
        List<String> topicsStudyingKeywords = KeywordExtractor.extractKeywords(request.getTopicsStudying());
        List<String> upcomingDeadlinesKeywords = KeywordExtractor.extractKeywords(request.getUpcomingDeadlines());
        
        if (existing != null) {
            // Update existing - only store keywords
            existing.setTheme(request.getTheme());
            existing.setLayoutPreference(request.getLayoutPreference());
            existing.setTopicsStudyingKeywords(topicsStudyingKeywords);
            existing.setUpcomingDeadlinesKeywords(upcomingDeadlinesKeywords);
            existing.setUpdatedAt(LocalDateTime.now());
            return workspaceRepository.save(existing);
        } else {
            // Create new - only store keywords
            Workspace workspace = Workspace.builder()
                    .userId(userId)
                    .theme(request.getTheme())
                    .layoutPreference(request.getLayoutPreference())
                    .topicsStudyingKeywords(topicsStudyingKeywords)
                    .upcomingDeadlinesKeywords(upcomingDeadlinesKeywords)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            return workspaceRepository.save(workspace);
        }
    }
}
