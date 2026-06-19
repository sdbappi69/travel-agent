package com.planet0088.aiagent;

import com.planet0088.aiagent.engine.security.config.JwtProperties;
import com.planet0088.aiagent.engine.config.CorsProperties;
import com.planet0088.aiagent.engine.config.SecurityProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({JwtProperties.class, CorsProperties.class, SecurityProperties.class})
public class AiAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiAgentApplication.class, args);
    }
}
