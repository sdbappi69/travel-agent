package com.planet0088.travelAgent.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "travelagent.cors")
@Data
public class CorsProperties {
    private List<String> allowedOrigins;
}
