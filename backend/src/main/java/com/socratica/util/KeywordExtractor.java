package com.socratica.util;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility class to extract keywords from text input.
 * Extracts meaningful words, removes common stop words, and returns a list of keywords.
 */
public class KeywordExtractor {
    
    // Common stop words to filter out
    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
        "a", "an", "and", "are", "as", "at", "be", "by", "for", "from",
        "has", "he", "in", "is", "it", "its", "of", "on", "that", "the",
        "to", "was", "will", "with", "the", "this", "but", "they", "have",
        "had", "what", "said", "each", "which", "their", "time", "if",
        "up", "out", "many", "then", "them", "these", "so", "some", "her",
        "would", "make", "like", "into", "him", "has", "two", "more",
        "very", "after", "words", "long", "than", "first", "been", "call",
        "who", "oil", "sit", "now", "find", "down", "day", "did", "get",
        "come", "made", "may", "part", "over", "new", "sound", "take",
        "only", "little", "work", "know", "place", "year", "live", "me",
        "back", "give", "most", "very", "after", "thing", "our", "just",
        "name", "good", "sentence", "man", "think", "say", "great", "where",
        "help", "through", "much", "before", "line", "right", "too", "mean",
        "old", "any", "same", "tell", "boy", "follow", "came", "want", "show",
        "also", "around", "form", "three", "small", "set", "put", "end",
        "does", "another", "well", "large", "must", "big", "even", "such",
        "because", "turn", "here", "why", "ask", "went", "men", "read",
        "need", "land", "different", "home", "us", "move", "try", "kind",
        "hand", "picture", "again", "change", "off", "play", "spell", "air",
        "away", "animal", "house", "point", "page", "letter", "mother", "answer",
        "found", "study", "still", "learn", "should", "America", "world", "high"
    ));
    
    /**
     * Extracts keywords from a text string.
     * Splits by common delimiters, removes stop words, and returns meaningful keywords.
     * 
     * @param text The input text to extract keywords from
     * @return List of extracted keywords (lowercase, unique)
     */
    public static List<String> extractKeywords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        // Split by common delimiters: comma, semicolon, space, newline
        String[] words = text.toLowerCase()
                .replaceAll("[,\\n\\r;]", " ")
                .split("\\s+");
        
        return Arrays.stream(words)
                .map(String::trim)
                .filter(word -> word.length() > 2) // Filter out very short words
                .filter(word -> !STOP_WORDS.contains(word)) // Remove stop words
                .filter(word -> word.matches("^[a-zA-Z]+$")) // Only alphabetic words
                .distinct() // Remove duplicates
                .sorted()
                .collect(Collectors.toList());
    }
    
    /**
     * Extracts keywords and returns them as a comma-separated string.
     * 
     * @param text The input text
     * @return Comma-separated keywords string
     */
    public static String extractKeywordsAsString(String text) {
        List<String> keywords = extractKeywords(text);
        return String.join(", ", keywords);
    }
}
