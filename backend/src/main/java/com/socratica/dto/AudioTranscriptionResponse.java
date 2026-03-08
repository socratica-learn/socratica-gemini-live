package com.socratica.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AudioTranscriptionResponse {
    private String transcript;
}
