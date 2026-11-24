package com.apihub.backend.features.auth.controller;

import com.apihub.backend.common.dto.ApiResponse;
import com.apihub.backend.features.auth.dto.AuthResponse;
import com.apihub.backend.features.auth.dto.LoginRequest;
import com.apihub.backend.features.auth.service.AuthService;
import com.apihub.backend.features.users.domain.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AuthResponse>> getCurrentUser(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthenticated"));
        }
        AuthResponse response = authService.getCurrentUser(user);
        return ResponseEntity.ok(ApiResponse.success(response, "User profile retrieved"));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        return ResponseEntity.ok(ApiResponse.success(null, "Logged out successfully"));
    }
}
