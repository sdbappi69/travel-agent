package com.planet0088.aiagent.engine.security.service.implement;

import com.planet0088.aiagent.engine.security.config.JwtProperties;
import com.planet0088.aiagent.engine.security.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtServiceImplement implements JwtService {

    private final JwtProperties jwtProperties;

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String generateToken(String staffUserId, String email, String tenantId, String tenantSlug, String role) {
        return Jwts.builder()
            .subject(staffUserId)
            .claim("email", email)
            .claim("tenantId", tenantId)
            .claim("tenantSlug", tenantSlug)
            .claim("role", role)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + jwtProperties.getExpirationMs()))
            .signWith(signingKey())
            .compact();
    }

    @Override
    public Claims validateAndExtract(String token) {
        return Jwts.parser()
            .verifyWith(signingKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    @Override
    public boolean isTokenValid(String token) {
        try {
            validateAndExtract(token);
            return true;
        } catch (Exception e) {
            log.debug("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String extractUserId(String token) {
        return validateAndExtract(token).getSubject();
    }

    @Override
    public String extractEmail(String token) {
        return validateAndExtract(token).get("email", String.class);
    }

    @Override
    public String extractTenantId(String token) {
        return validateAndExtract(token).get("tenantId", String.class);
    }

    @Override
    public String extractTenantSlug(String token) {
        return validateAndExtract(token).get("tenantSlug", String.class);
    }

    @Override
    public String extractRole(String token) {
        return validateAndExtract(token).get("role", String.class);
    }
}
