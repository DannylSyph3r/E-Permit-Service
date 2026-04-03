package dev.slethware.epermitservice.controller;

import dev.slethware.epermitservice.model.dto.request.LoginRequest;
import dev.slethware.epermitservice.model.dto.request.RegisterRequest;
import dev.slethware.epermitservice.model.dto.response.ApiResponse;
import dev.slethware.epermitservice.model.dto.response.AuthResponse;
import dev.slethware.epermitservice.service.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user registration and token generation")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(
            summary = "Register a new user",
            description = "Create a new account. The tenantId must be a whitelisted value."
    )
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/token")
    @Operation(
            summary = "Obtain a JWT",
            description = "Authenticate with email and password to receive a JWT for subsequent requests."
    )
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}