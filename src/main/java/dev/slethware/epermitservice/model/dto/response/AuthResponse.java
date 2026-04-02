package dev.slethware.epermitservice.model.dto.response;

import lombok.Builder;

@Builder
public record AuthResponse(String token) {}