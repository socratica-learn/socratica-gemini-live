package com.socratica.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentReviewResponse {
    /** Detected document type: presentation, research_paper, report, essay, notes, other */
    private String documentType;

    /** Human-readable label, e.g. "Academic Research Paper" */
    private String documentTypeLabel;

    /** Short 2-3 sentence overview of the document */
    private String summary;

    private List<String> strengths;
    private List<String> weaknesses;
    private List<String> suggestions;

    /**
     * Per-dimension scores 1-10.
     * Keys: clarity, structure, comprehensiveness, coherence, relevance, depth
     */
    private Map<String, Integer> qualityScores;
}
