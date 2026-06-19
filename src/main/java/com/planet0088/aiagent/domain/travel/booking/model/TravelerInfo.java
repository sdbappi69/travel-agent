package com.planet0088.aiagent.domain.travel.booking.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TravelerInfo {
    private int adults;
    private int children;
    private List<String> nationalities;
}
