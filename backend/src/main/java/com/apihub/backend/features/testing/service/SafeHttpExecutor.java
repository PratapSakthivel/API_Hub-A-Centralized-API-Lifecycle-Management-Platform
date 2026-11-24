package com.apihub.backend.features.testing.service;

import com.apihub.backend.features.testing.dto.HttpExecutionResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class SafeHttpExecutor {

    private final SafeHttpDestinationValidator validator;

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();

    public HttpExecutionResult execute(String method, String url, String body, int expectedStatus) {
        validator.validateUrl(url);

        long start = System.currentTimeMillis();
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .header("User-Agent", "ApiHub-TestingEngine/1.0");

            if ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method) || "PATCH".equalsIgnoreCase(method)) {
                builder.method(method.toUpperCase(), HttpRequest.BodyPublishers.ofString(body != null ? body : ""));
                builder.header("Content-Type", "application/json");
            } else if ("DELETE".equalsIgnoreCase(method)) {
                builder.DELETE();
            } else {
                builder.GET();
            }

            HttpResponse<String> response = CLIENT.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            long duration = System.currentTimeMillis() - start;

            int actualStatus = response.statusCode();
            boolean passed = (actualStatus == expectedStatus);

            // Sanitize & redact sensitive response data
            String sanitized = response.body();
            if (sanitized != null && sanitized.length() > 2000) {
                sanitized = sanitized.substring(0, 2000) + "... [truncated]";
            }

            return HttpExecutionResult.builder()
                    .statusCode(actualStatus)
                    .durationMs(duration)
                    .responseBody(sanitized)
                    .successful(passed)
                    .failureReason(passed ? null : "Expected status " + expectedStatus + " but received " + actualStatus)
                    .build();

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - start;
            return HttpExecutionResult.builder()
                    .statusCode(0)
                    .durationMs(duration)
                    .successful(false)
                    .failureReason(e.getMessage() != null ? e.getMessage() : "HTTP execution failed")
                    .build();
        }
    }
}
