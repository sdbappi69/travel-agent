package com.planet0088.travelAgent.auth;

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
public class JwtService {

    private final JwtProperties jwtProperties;

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

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

    public Claims validateAndExtract(String token) {
        return Jwts.parser()
            .verifyWith(signingKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    public boolean isTokenValid(String token) {
        try {
            validateAndExtract(token);
            return true;
        } catch (Exception e) {
            log.debug("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public String extractUserId(String token) {
        return validateAndExtract(token).getSubject();
    }

    public String extractEmail(String token) {
        return validateAndExtract(token).get("email", String.class);
    }

    public String extractTenantId(String token) {
        return validateAndExtract(token).get("tenantId", String.class);
    }

    public String extractTenantSlug(String token) {
        return validateAndExtract(token).get("tenantSlug", String.class);
    }

    public String extractRole(String token) {
        return validateAndExtract(token).get("role", String.class);
    }
}
