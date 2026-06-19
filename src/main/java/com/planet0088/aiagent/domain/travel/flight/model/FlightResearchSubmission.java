package com.planet0088.aiagent.domain.travel.flight.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlightResearchSubmission {
    private List<FlightOption> options;
    private String staffNotes;
}
