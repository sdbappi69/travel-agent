package com.planet0088.travelAgent.config;

public final class TenantContext {

    private static final ThreadLocal<String> HOLDER = new ThreadLocal<>();

    private TenantContext() {}

    public static void set(String tenantId) {
        HOLDER.set(tenantId);
    }

    public static String get() {
        String tenantId = HOLDER.get();
        if (tenantId == null) {
            throw new IllegalStateException("No tenant context set for current thread");
        }
        return tenantId;
    }

    public static boolean has() {
        return HOLDER.get() != null;
    }

    public static void clear() {
        HOLDER.remove();
    }
}
