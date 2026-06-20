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

    // WARNING: not tenant-scoped — only safe to call when the caller has no
    // other way to know the tenant yet (e.g. resolving tenant FROM the bookingId
    // itself, such as in the unauthenticated SSE stream endpoint).
    // Do not use this for any operation where tenant isolation must be enforced —
    // use getByIdForTenant(tenantId, bookingId) instead.
    Booking getById(String bookingId);
}
