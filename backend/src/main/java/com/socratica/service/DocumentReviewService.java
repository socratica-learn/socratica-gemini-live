package com.socratica.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.socratica.dto.DocumentReviewResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hslf.usermodel.HSLFTextParagraph;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DocumentReviewService uses the Gemini API (Google GenAI) to analyze uploaded
 * documents.
 *
 * <p>
 * Google stack used:
 * <ul>
 * <li>Gemini 2.5 Flash via the Google Generative Language REST API</li>
 * </ul>
 *
 * <p>
 * Supported file types: PDF, DOCX, PPTX, PPTX (legacy .ppt), plain text.
 * Text is extracted server-side and sent to Gemini for context-aware review.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentReviewService {

    private final ObjectMapper objectMapper;
    private final Client geminiClient;

    @Value("${socratica.gemini.model:gemini-3-flash-preview}")
    private String geminiModel;

    private static final int MAX_CONTENT_CHARS = 30000;

    /**
     * Extracts plain text from the uploaded file without running a Gemini review.
     * Used by Interview Prep to feed CV content into the live session system
     * prompt.
     */
    public String extractText(MultipartFile file) {
        String filename = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String contentType = file.getContentType() == null ? "" : file.getContentType();
        String extension = extractExtension(filename).toLowerCase();
        String text = extractText(file, extension, contentType);
        if (text.length() > MAX_CONTENT_CHARS) {
            text = text.substring(0, MAX_CONTENT_CHARS) + "\n[... content truncated ...]";
        }
        return text;
    }

    /**
     * Extracts text from the uploaded file, sends it to Gemini for structured
     * review,
     * and returns a {@link DocumentReviewResponse}.
     */
    public DocumentReviewResponse reviewDocument(MultipartFile file) {
        String filename = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String contentType = file.getContentType() == null ? "" : file.getContentType();
        String extension = extractExtension(filename).toLowerCase();

        String extractedText = extractText(file, extension, contentType);
        if (extractedText.isBlank()) {
            throw new RuntimeException("Could not extract text from the uploaded document.");
        }

        // Truncate to avoid exceeding Gemini context limits
        if (extractedText.length() > MAX_CONTENT_CHARS) {
            extractedText = extractedText.substring(0, MAX_CONTENT_CHARS) + "\n[... content truncated ...]";
        }

        String prompt = buildPrompt(extractedText, filename);
        String raw = callGemini(prompt);
        return parseGeminiResponse(raw);
    }

    // -------------------------------------------------------------------------
    // Text extraction
    // -------------------------------------------------------------------------

    private String extractText(MultipartFile file, String extension, String contentType) {
        try (InputStream in = file.getInputStream()) {
            return switch (extension) {
                case "pdf" -> extractPdf(in);
                case "docx" -> extractDocx(in);
                case "pptx" -> extractPptx(in);
                case "ppt" -> extractPpt(in);
                case "txt", "md", "rst", "csv" -> new String(in.readAllBytes(), StandardCharsets.UTF_8);
                default -> {
                    if (contentType.contains("pdf")) {
                        yield extractPdf(file.getInputStream());
                    } else if (contentType.contains("text")) {
                        yield new String(file.getBytes(), StandardCharsets.UTF_8);
                    }
                    throw new RuntimeException("Unsupported file type: " + extension);
                }
            };
        } catch (IOException e) {
            throw new RuntimeException("Failed to read uploaded file: " + e.getMessage(), e);
        }
    }

    private String extractPdf(InputStream in) throws IOException {
        try (PDDocument doc = Loader.loadPDF(in.readAllBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(doc);
        }
    }

    private String extractDocx(InputStream in) throws IOException {
        try (XWPFDocument doc = new XWPFDocument(in);
                XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {
            return extractor.getText();
        }
    }

    private String extractPptx(InputStream in) throws IOException {
        try (XMLSlideShow pptx = new XMLSlideShow(in)) {
            StringBuilder sb = new StringBuilder();
            for (XSLFSlide slide : pptx.getSlides()) {
                sb.append("--- Slide ").append(slide.getSlideNumber()).append(" ---\n");
                for (XSLFShape shape : slide.getShapes()) {
                    if (shape instanceof XSLFTextShape textShape) {
                        String text = textShape.getText();
                        if (text != null && !text.isBlank()) {
                            sb.append(text).append("\n");
                        }
                    }
                }
            }
            return sb.toString();
        }
    }

    private String extractPpt(InputStream in) throws IOException {
        try (HSLFSlideShow ppt = new HSLFSlideShow(in)) {
            StringBuilder sb = new StringBuilder();
            ppt.getSlides().forEach(slide -> {
                sb.append("--- Slide ").append(slide.getSlideNumber()).append(" ---\n");
                slide.getTextParagraphs().forEach(paragraphs -> {
                    String text = HSLFTextParagraph.getText(paragraphs);
                    if (text != null && !text.isBlank()) {
                        sb.append(text).append("\n");
                    }
                });
            });
            return sb.toString();
        }
    }

    // -------------------------------------------------------------------------
    // Gemini interaction
    // -------------------------------------------------------------------------

    private String buildPrompt(String content, String filename) {
        return """
                You are an expert academic and professional document reviewer powered by Google Gemini.
                Analyze the document below and return a structured JSON review.

                Filename: %s

                Document content:
                ---
                %s
                ---

                Detection criteria:
                - "presentation": contains slide markers, bullet-heavy structure, short text blocks
                - "research_paper": has abstract/introduction/methodology/results/conclusion, citations
                - "report": formal sections, executive summary, findings, recommendations
                - "essay": continuous prose, thesis-driven, argumentative structure
                - "notes": informal, fragmented, outline-style

                Review criteria by type:
                - presentation: slide clutter, text density per slide, visual clarity, narrative flow,
                  engagement, readability, structure across slides
                - research_paper: abstract quality, argument rigor, methodology clarity, section balance,
                  citation usage, contribution clarity
                - report: executive summary quality, findings clarity, recommendation specificity,
                  section balance, readability, professional tone
                - essay: thesis clarity, argumentation, evidence quality, flow, writing quality
                - notes: organization, completeness, clarity, usefulness for review

                Return ONLY a valid JSON object (no markdown fences, no extra text) with this exact structure:
                {
                  "documentType": "<presentation|research_paper|report|essay|notes|other>",
                  "documentTypeLabel": "<e.g. 'Academic Research Paper'>",
                  "summary": "<2-3 sentence overview of the document content and purpose>",
                  "strengths": ["<strength 1>", "<strength 2>", "..."],
                  "weaknesses": ["<weakness 1>", "<weakness 2>", "..."],
                  "suggestions": ["<actionable suggestion 1>", "<actionable suggestion 2>", "..."],
                  "qualityScores": {
                    "clarity": <integer 1-10>,
                    "structure": <integer 1-10>,
                    "comprehensiveness": <integer 1-10>,
                    "coherence": <integer 1-10>,
                    "relevance": <integer 1-10>,
                    "depth": <integer 1-10>
                  }
                }
                """.formatted(filename, content);
    }

    private String callGemini(String prompt) {
        try {
            GenerateContentConfig config = GenerateContentConfig.builder()
                    .responseMimeType("application/json")
                    .build();
            GenerateContentResponse response = geminiClient.models.generateContent(geminiModel, prompt, config);
            String text = response.text();
            if (text == null) {
                throw new RuntimeException("Unexpected empty response from Gemini");
            }
            return text;
        } catch (Exception e) {
            log.error("Gemini API call failed: {}", e.getMessage());
            throw new RuntimeException("Failed to call Gemini API", e);
        }
    }

    private DocumentReviewResponse parseGeminiResponse(String raw) {
        try {
            // Strip accidental markdown fences if present
            String json = raw.trim();
            if (json.startsWith("```")) {
                json = json.replaceAll("```[a-z]*\\n?", "").replaceAll("```", "").trim();
            }

            JsonNode node = objectMapper.readTree(json);

            DocumentReviewResponse response = new DocumentReviewResponse();
            response.setDocumentType(node.path("documentType").asText("other"));
            response.setDocumentTypeLabel(node.path("documentTypeLabel").asText("Document"));
            response.setSummary(node.path("summary").asText(""));

            response.setStrengths(toStringList(node.path("strengths")));
            response.setWeaknesses(toStringList(node.path("weaknesses")));
            response.setSuggestions(toStringList(node.path("suggestions")));

            Map<String, Integer> scores = new HashMap<>();
            JsonNode scoresNode = node.path("qualityScores");
            scoresNode.fields().forEachRemaining(e -> scores.put(e.getKey(), e.getValue().asInt(5)));
            response.setQualityScores(scores);

            return response;
        } catch (Exception e) {
            log.error("Failed to parse Gemini document review response: {}", e.getMessage());
            throw new RuntimeException("Failed to parse Gemini review response", e);
        }
    }

    private List<String> toStringList(JsonNode node) {
        List<String> list = new ArrayList<>();
        if (node.isArray()) {
            node.forEach(item -> list.add(item.asText()));
        }
        return list;
    }

    private String extractExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot + 1) : "";
    }
}
