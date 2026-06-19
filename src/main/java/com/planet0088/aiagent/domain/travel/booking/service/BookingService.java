package com.planet0088.aiagent.domain.travel.booking.service;

import com.planet0088.aiagent.domain.travel.booking.model.Booking;
import com.planet0088.aiagent.domain.travel.booking.model.ClientInfo;
import com.planet0088.aiagent.domain.travel.booking.model.TravelerInfo;
import com.planet0088.aiagent.domain.travel.booking.model.TripDetails;

public interface BookingService {

    Booking createSession(String tenantId, String sessionId);

    Booking finalizeIntake(String tenantId, String bookingId,
                           ClientInfo clientInfo,
                           TravelerInfo travelerInfo,
                           TripDetails tripDetails);

    Booking getByIdForTenant(String tenantId, String bookingId);
}
