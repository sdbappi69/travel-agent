package com.planet0088.travelAgent.tenant;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterTenantRequest {

    @NotBlank
    private String name;

    @NotBlank
    @Pattern(regexp = "^[a-z0-9-]+$", message = "slug must contain only lowercase letters, numbers, and hyphens")
    @Size(min = 3, max = 30)
    private String slug;

    @Email
    @NotBlank
    private String ownerEmail;

    @NotBlank
    @Size(min = 8)
    private String ownerPassword;

    private String timezone;
    private String currency;
    private String agentName;
}
