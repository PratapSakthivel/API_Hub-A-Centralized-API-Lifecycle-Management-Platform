package com.apihub.backend.features.testing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HttpExecutionResult {
    private int statusCode;
    private long durationMs;
    private String responseBody;
    private boolean successful;
    private String failureReason;
}
