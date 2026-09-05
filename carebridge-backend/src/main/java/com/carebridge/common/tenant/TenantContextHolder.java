package com.carebridge.common.tenant;

import java.util.Optional;
import java.util.UUID;

public final class TenantContextHolder {

    private static final ThreadLocal<TenantContext> CURRENT = new ThreadLocal<>();

    private TenantContextHolder() {
    }

    public static void set(TenantContext context) {
        CURRENT.set(context);
    }

    public static void clear() {
        CURRENT.remove();
    }

    public static Optional<TenantContext> get() {
        return Optional.ofNullable(CURRENT.get());
    }

    public static TenantContext require() {
        TenantContext context = CURRENT.get();
        if (context == null) {
            throw new IllegalStateException("tenant context is not available");
        }
        return context;
    }

    public static UUID requireTenantId() {
        return require().requireTenantId();
    }
}
