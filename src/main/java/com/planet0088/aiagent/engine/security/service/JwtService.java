package com.planet0088.aiagent.engine.security.service;

import io.jsonwebtoken.Claims;

public interface JwtService {

    String generateToken(String staffUserId, String email, String tenantId, String tenantSlug, String role);

    Claims validateAndExtract(String token);

    boolean isTokenValid(String token);

    String extractUserId(String token);

    String extractEmail(String token);

    String extractTenantId(String token);

    String extractTenantSlug(String token);

    String extractRole(String token);
}
