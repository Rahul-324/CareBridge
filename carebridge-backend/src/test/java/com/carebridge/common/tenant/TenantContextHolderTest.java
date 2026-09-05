package com.carebridge.common.tenant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.carebridge.auth.entity.UserRole;

class TenantContextHolderTest {

    @AfterEach
    void clear() {
        TenantContextHolder.clear();
    }

    @Test
    void requireTenantIdReturnsBoundValue() {
        UUID tenantId = UUID.randomUUID();
        TenantContextHolder.set(new TenantContext(
                UUID.randomUUID(),
                tenantId,
                null,
                UserRole.CLINIC_ADMIN,
                "admin@clinic.test"
        ));

        assertEquals(tenantId, TenantContextHolder.requireTenantId());
        assertTrue(TenantContextHolder.get().isPresent());
    }

    @Test
    void requireFailsWhenContextMissing() {
        assertThrows(IllegalStateException.class, TenantContextHolder::require);
    }
}
