package com.planet0088.travelAgent.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "travelagent.security")
@Data
public class SecurityProperties {

    private PermitAll permitAll = new PermitAll();

    @Data
    public static class PermitAll {
        private List<String> post = new ArrayList<>();
        private List<String> get  = new ArrayList<>();
        private List<String> any  = new ArrayList<>();
    }
}
