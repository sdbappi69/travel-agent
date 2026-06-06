package com.planet0088.travelAgent.tenant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "tenants")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tenant {

    @Id
    private String id;

    @Indexed(unique = true)
    private String slug;

    private String name;
    private String ownerEmail;
    private String status;
    private TenantSettings settings;
    private Instant createdAt;
    private Instant updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TenantSettings {
        private String timezone;
        private String currency;
        private String primaryColor;
        private String logoUrl;
        private String whatsappNumber;
        private String supportEmail;
        private String agentName;
    }
}
