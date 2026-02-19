package com.socratica.controller;

import com.socratica.dto.AiResponse;
import com.socratica.dto.JobPreparationCoverLetterRequest;
import com.socratica.dto.JobPreparationCvRequest;
import com.socratica.dto.JobPreparationInterviewRequest;
import com.socratica.dto.NotesSummariesRequest;
import com.socratica.dto.PresentationPrepRequest;
import com.socratica.dto.SocraticInterruptionRequest;
import com.socratica.dto.SocraticSessionRequest;
import com.socratica.dto.TeachingAdviceRequest;
import com.socratica.dto.WrittenEvaluationRequest;
import com.socratica.service.AiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {
    private final AiService aiService;

    @PostMapping("/written-evaluation/quizzes")
    public ResponseEntity<AiResponse> customizeQuizzes(@Valid @RequestBody WrittenEvaluationRequest request) {
        return ResponseEntity.ok(aiService.generateWrittenEvaluation("quizzes", request));
    }

    @PostMapping("/written-evaluation/exams")
    public ResponseEntity<AiResponse> customizeExams(@Valid @RequestBody WrittenEvaluationRequest request) {
        return ResponseEntity.ok(aiService.generateWrittenEvaluation("exams", request));
    }

    @PostMapping("/written-evaluation/tests")
    public ResponseEntity<AiResponse> customizeTests(@Valid @RequestBody WrittenEvaluationRequest request) {
        return ResponseEntity.ok(aiService.generateWrittenEvaluation("tests", request));
    }

    @PostMapping("/written-evaluation/custom")
    public ResponseEntity<AiResponse> customizeWrittenEvaluation(@Valid @RequestBody WrittenEvaluationRequest request) {
        return ResponseEntity.ok(aiService.generateWrittenEvaluation("custom", request));
    }

    @PostMapping("/socratic-evaluation/session")
    public ResponseEntity<AiResponse> startSocraticSession(@Valid @RequestBody SocraticSessionRequest request) {
        return ResponseEntity.ok(aiService.startSocraticSession(request));
    }

    @PostMapping("/socratic-evaluation/interruption")
    public ResponseEntity<AiResponse> generateSocraticInterruption(@Valid @RequestBody SocraticInterruptionRequest request) {
        return ResponseEntity.ok(aiService.generateSocraticInterruption(request));
    }

    @PostMapping("/presentation-prep/analyze")
    public ResponseEntity<AiResponse> analyzePresentation(@Valid @RequestBody PresentationPrepRequest request) {
        return ResponseEntity.ok(aiService.analyzePresentation(request));
    }

    @PostMapping("/job-preparation/cover-letter")
    public ResponseEntity<AiResponse> analyzeCoverLetter(@Valid @RequestBody JobPreparationCoverLetterRequest request) {
        return ResponseEntity.ok(aiService.analyzeCoverLetter(request));
    }

    @PostMapping("/job-preparation/cv")
    public ResponseEntity<AiResponse> analyzeCv(@Valid @RequestBody JobPreparationCvRequest request) {
        return ResponseEntity.ok(aiService.analyzeCv(request));
    }

    @PostMapping("/job-preparation/interview")
    public ResponseEntity<AiResponse> prepareInterview(@Valid @RequestBody JobPreparationInterviewRequest request) {
        return ResponseEntity.ok(aiService.prepareInterview(request));
    }

    @PostMapping("/notes-summaries")
    public ResponseEntity<AiResponse> generateNotesAndSummaries(@Valid @RequestBody NotesSummariesRequest request) {
        return ResponseEntity.ok(aiService.generateNotesAndSummaries(request));
    }

    @PostMapping("/teaching-advice")
    public ResponseEntity<AiResponse> generateTeachingAdvice(@Valid @RequestBody TeachingAdviceRequest request) {
        return ResponseEntity.ok(aiService.generateTeachingAdvice(request));
    }
}
