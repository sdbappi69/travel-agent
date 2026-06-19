package com.planet0088.aiagent.engine.security.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "travelagent.jwt")
@Data
public class JwtProperties {
    private String secret;
    private long expirationMs;
}
