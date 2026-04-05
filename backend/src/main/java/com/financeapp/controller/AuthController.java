package com.financeapp.controller;

import com.financeapp.dto.request.LoginRequest;
import com.financeapp.dto.request.RegisterRequest;
import com.financeapp.dto.response.ApiResponse;
import com.financeapp.dto.response.AuthResponse;
import com.financeapp.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * POST /api/auth/login
     * Authenticate and receive a JWT token.
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        AuthResponse auth = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok("Login successful.", auth));
    }

    /**
     * POST /api/auth/register
     * Register a new user (ADMIN can set role; default is VIEWER).
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        AuthResponse auth = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok("User registered successfully.", auth));
    }
}
