package com.planet0088.aiagent.engine.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "travelagent.cors")
@Data
public class CorsProperties {
    private List<String> allowedOrigins;
}
