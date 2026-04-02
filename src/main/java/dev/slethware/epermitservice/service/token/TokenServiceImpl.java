package dev.slethware.epermitservice.service.token;

import dev.slethware.epermitservice.model.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private final SecretKey jwtSecretKey;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    @Override
    public String generateAccessToken(Authentication authentication, User user) {
        Date now = new Date();
        Date expiresAt = new Date(now.getTime() + jwtExpiration);

        String scope = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId().toString());
        claims.put("scope", scope);

        return Jwts.builder()
                .claims(claims)
                .subject(user.getEmail())
                .issuedAt(now)
                .expiration(expiresAt)
                .signWith(jwtSecretKey, Jwts.SIG.HS256)
                .compact();
    }

    @Override
    public String extractEmail(String token) {
        try {
            return parseClaims(token).getSubject();
        } catch (Exception e) {
            log.error("Failed to extract email from token: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public boolean validateAccessToken(String token, String email) {
        try {
            Claims claims = parseClaims(token);
            boolean notExpired = !claims.getExpiration().before(new Date());
            boolean emailMatches = claims.getSubject().equals(email);
            return notExpired && emailMatches;
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(jwtSecretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}