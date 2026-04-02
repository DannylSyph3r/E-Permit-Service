package dev.slethware.epermitservice.service.token;

import dev.slethware.epermitservice.model.entity.User;
import org.springframework.security.core.Authentication;

public interface TokenService {
    String generateAccessToken(Authentication authentication, User user);
    String extractEmail(String token);
    boolean validateAccessToken(String token, String email);
}