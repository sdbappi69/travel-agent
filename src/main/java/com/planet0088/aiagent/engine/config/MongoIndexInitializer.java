package com.planet0088.aiagent.engine.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MongoIndexInitializer implements CommandLineRunner {

    private final MongoTemplate mongoTemplate;

    @Override
    public void run(String... args) {
        ensureStaffUsersIndexes();
        ensureBookingsIndexes();
        ensureManualTasksIndexes();
    }

    private void ensureStaffUsersIndexes() {
        mongoTemplate.indexOps("staff_users").ensureIndex(
            new Index()
                .on("tenantId", Sort.Direction.ASC)
                .on("email", Sort.Direction.ASC)
                .unique()
                .named("idx_staff_tenantId_email")
        );
        log.debug("Ensured index staff_users(tenantId, email) UNIQUE");
    }

    private void ensureBookingsIndexes() {
        mongoTemplate.indexOps("bookings").ensureIndex(
            new Index()
                .on("tenantId", Sort.Direction.ASC)
                .on("status", Sort.Direction.ASC)
                .named("idx_bookings_tenantId_status")
        );
        mongoTemplate.indexOps("bookings").ensureIndex(
            new Index()
                .on("tenantId", Sort.Direction.ASC)
                .on("createdAt", Sort.Direction.DESC)
                .named("idx_bookings_tenantId_createdAt")
        );
        log.debug("Ensured indexes on bookings(tenantId, status) and (tenantId, createdAt desc)");
    }

    private void ensureManualTasksIndexes() {
        mongoTemplate.indexOps("manual_tasks").ensureIndex(
            new Index()
                .on("tenantId", Sort.Direction.ASC)
                .on("status", Sort.Direction.ASC)
                .named("idx_manual_tasks_tenantId_status")
        );
        mongoTemplate.indexOps("manual_tasks").ensureIndex(
            new Index()
                .on("tenantId", Sort.Direction.ASC)
                .on("bookingId", Sort.Direction.ASC)
                .named("idx_manual_tasks_tenantId_bookingId")
        );
        log.debug("Ensured indexes on manual_tasks(tenantId, status) and (tenantId, bookingId)");
    }
}
