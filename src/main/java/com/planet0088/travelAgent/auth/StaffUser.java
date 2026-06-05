package com.planet0088.travelAgent.auth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "staff_users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffUser {

    @Id
    private String id;

    @Indexed
    private String tenantId;

    private String tenantSlug;

    @Indexed
    private String email;

    @JsonIgnore
    private String passwordHash;

    private String role;
    private String fullName;
    private boolean active;
    private Instant createdAt;
    private Instant lastLoginAt;
}
