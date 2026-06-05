package com.planet0088.travelAgent.auth;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "travelagent.jwt")
@Data
public class JwtProperties {
    private String secret;
    private long expirationMs;
}
