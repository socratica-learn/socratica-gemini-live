package com.socratica.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SocraticTutorMessage {
    private String speaker;
    private String text;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
