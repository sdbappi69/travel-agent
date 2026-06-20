package com.planet0088.aiagent.engine.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "travelagent.security")
@Data
public class SecurityProperties {

    private PermitAll permitAll = new PermitAll();
    private Roles roles = new Roles();

    @Data
    public static class PermitAll {
        private List<String> post = new ArrayList<>();
        private List<String> get  = new ArrayList<>();
        private List<String> any  = new ArrayList<>();
    }

    @Data
    public static class Roles {
        private List<String> staff = new ArrayList<>();
    }
}
