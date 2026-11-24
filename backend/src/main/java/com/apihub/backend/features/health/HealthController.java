package com.apihub.backend.features.health;

import com.apihub.backend.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getHealth() {
        Map<String, Object> healthInfo = Map.of(
                "status", "UP",
                "service", "api-hub-backend",
                "version", "1.0.0",
                "lifecycle", "operational"
        );
        return ResponseEntity.ok(ApiResponse.success(healthInfo, "API Hub Backend is running"));
    }
}
