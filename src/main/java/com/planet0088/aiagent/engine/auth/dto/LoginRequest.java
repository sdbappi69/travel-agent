package com.planet0088.aiagent.engine.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank
    private String tenantSlug;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;
}
