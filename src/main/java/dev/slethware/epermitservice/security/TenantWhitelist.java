package dev.slethware.epermitservice.security;

import java.util.Set;

public final class TenantWhitelist {

    public static final String MINISTRY_HEALTH    = "Ministry_Health";
    public static final String MINISTRY_EDUCATION = "Ministry_Education";

    private static final Set<String> VALID_TENANTS = Set.of(
            MINISTRY_HEALTH,
            MINISTRY_EDUCATION
    );

    private TenantWhitelist() {}

    public static boolean isValid(String tenantId) {
        return tenantId != null && VALID_TENANTS.contains(tenantId);
    }
}