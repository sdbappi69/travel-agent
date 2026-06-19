package com.planet0088.aiagent.domain.travel.booking.repository;

import com.planet0088.aiagent.domain.travel.booking.model.Booking;
import com.planet0088.aiagent.domain.travel.booking.model.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends MongoRepository<Booking, String> {
    Optional<Booking> findByTenantIdAndId(String tenantId, String id);
    Optional<Booking> findByTenantIdAndSessionId(String tenantId, String sessionId);
    Page<Booking> findByTenantId(String tenantId, Pageable pageable);
    List<Booking> findByTenantIdAndStatus(String tenantId, BookingStatus status);
}
