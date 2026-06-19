package com.planet0088.aiagent.domain.travel.booking.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientInfo {
    private String name;
    private String email;
    private String phone;
}
