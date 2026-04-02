package dev.slethware.epermitservice.service.auth;

import dev.slethware.epermitservice.model.dto.request.LoginRequest;
import dev.slethware.epermitservice.model.dto.request.RegisterRequest;
import dev.slethware.epermitservice.model.dto.response.ApiResponse;
import dev.slethware.epermitservice.model.dto.response.AuthResponse;

public interface AuthService {
    ApiResponse<Void> register(RegisterRequest request);
    ApiResponse<AuthResponse> login(LoginRequest request);
}