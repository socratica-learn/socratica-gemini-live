package com.socratica.service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiService {

    private final Client geminiClient;

    @Value("${socratica.gemini.model:gemini-2.5-flash}")
    private String geminiModel;

    public AiResponse generateWrittenEvaluation(String type, WrittenEvaluationRequest request) {
        String prompt = String.format(
            "Create %s for the topic: %s.\nQuestion count: %d.\nDifficulty: %s.\nFormat: %s.\nInstructions: %s.",
            type,
            request.getTopic(),
            request.getQuestionCount(),
            nullToDefault(request.getDifficulty(), "auto"),
            nullToDefault(request.getFormat(), "mixed"),
            nullToDefault(request.getInstructions(), "none")
        );
        return new AiResponse(generateContent(prompt));
    }

    public AiResponse startSocraticSession(SocraticSessionRequest request) {
        String prompt = String.format(
            "Start a 1:1 Socratic tutoring session on: %s.\nStudent level: %s.\nGoals: %s.\nStyle: %s.\nContext: %s.",
            request.getTopic(),
            nullToDefault(request.getStudentLevel(), "unknown"),
            nullToDefault(request.getGoals(), "none"),
            nullToDefault(request.getConversationStyle(), "curious"),
            nullToDefault(request.getContext(), "none")
        );
        return new AiResponse(generateContent(prompt));
    }

    public AiResponse generateSocraticInterruption(SocraticInterruptionRequest request) {
        String prompt = String.format(
            "Generate a Socratic interruption question based on this session context: %s\nLast user message: %s\nGoal: %s.",
            request.getSessionContext(),
            request.getLastUserMessage(),
            nullToDefault(request.getInterruptionGoal(), "clarify understanding")
        );
        return new AiResponse(generateContent(prompt));
    }

    public AiResponse analyzePresentation(PresentationPrepRequest request) {
        String prompt = String.format(
            "Analyze this presentation transcript for tone, slide quality, pace, engagement, and clarity.\nTranscript: %s\nSlide notes: %s\nAudience: %s\nGoals: %s.",
            request.getTranscript(),
            nullToDefault(request.getSlideNotes(), "none"),
            nullToDefault(request.getAudience(), "general"),
            nullToDefault(request.getGoals(), "improve delivery")
        );
        return new AiResponse(generateContent(prompt));
    }

    public AiResponse analyzeCoverLetter(JobPreparationCoverLetterRequest request) {
        String prompt = String.format(
            "Analyze this cover letter and suggest improvements.\nCover letter: %s\nJob description: %s\nFocus areas: %s.",
            request.getCoverLetterText(),
            nullToDefault(request.getJobDescription(), "none"),
            nullToDefault(request.getFocusAreas(), "clarity, impact")
        );
        return new AiResponse(generateContent(prompt));
    }

    public AiResponse analyzeCv(JobPreparationCvRequest request) {
        String prompt = String.format(
            "Review this CV/resume and suggest improvements.\nResume: %s\nTarget role: %s\nFocus areas: %s.",
            request.getResumeText(),
            nullToDefault(request.getTargetRole(), "general"),
            nullToDefault(request.getFocusAreas(), "structure, impact")
        );
        return new AiResponse(generateContent(prompt));
    }

    public AiResponse prepareInterview(JobPreparationInterviewRequest request) {
        String prompt = String.format(
            "Prepare interview questions and feedback for role: %s.\nJob description: %s\nResume: %s\nInterview type: %s\nDifficulty: %s.",
            request.getRole(),
            nullToDefault(request.getJobDescription(), "none"),
            nullToDefault(request.getResumeText(), "none"),
            nullToDefault(request.getInterviewType(), "mixed"),
            nullToDefault(request.getDifficulty(), "auto")
        );
        return new AiResponse(generateContent(prompt));
    }

    public AiResponse generateNotesAndSummaries(NotesSummariesRequest request) {
        String prompt = String.format(
            "Generate notes and summaries for the following content.\nContent: %s\nDesired length: %s\nFormat: %s\nFocus areas: %s.",
            request.getSourceText(),
            nullToDefault(request.getDesiredLength(), "concise"),
            nullToDefault(request.getFormat(), "bullet points"),
            nullToDefault(request.getFocusAreas(), "key points")
        );
        return new AiResponse(generateContent(prompt));
    }

    public AiResponse generateTeachingAdvice(TeachingAdviceRequest request) {
        String prompt = String.format(
            "Provide teaching advice for this topic.\nTopic: %s\nAudience: %s\nConstraints: %s\nTone: %s.",
            request.getTopic(),
            nullToDefault(request.getAudience(), "general"),
            nullToDefault(request.getConstraints(), "none"),
            nullToDefault(request.getTone(), "clear and supportive")
        );
        return new AiResponse(generateContent(prompt));
    }

    private String generateContent(String prompt) {
        try {
            GenerateContentResponse response = geminiClient.models.generateContent(geminiModel, prompt, null);
            String text = response.text();
            return text != null ? text : "";
        } catch (Exception e) {
            log.error("Gemini generateContent failed: {}", e.getMessage());
            throw new RuntimeException("Failed to call Gemini API", e);
        }
    }

    private String nullToDefault(String value, String defaultValue) {
        return (value == null || value.isBlank()) ? defaultValue : value;
    }
}
