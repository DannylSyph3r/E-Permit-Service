package dev.slethware.epermitservice.security;

import dev.slethware.epermitservice.model.entity.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class TenantValidationFilter extends OncePerRequestFilter {

    private static final String TENANT_HEADER = "X-Tenant-ID";

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/auth/")
                || path.startsWith("/internal/")
                || path.startsWith("/docs")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/api-docs")
                || path.startsWith("/swagger-ui");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String tenantId = request.getHeader(TENANT_HEADER);

        if (!TenantWhitelist.isValid(tenantId)) {
            log.warn("Rejected request — invalid or missing X-Tenant-ID: '{}'", tenantId);
            rejectWith(response, HttpServletResponse.SC_BAD_REQUEST,
                    "Missing or invalid X-Tenant-ID header. Valid values: Ministry_Health, Ministry_Education");
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User user) {
            if (!user.getTenantId().equals(tenantId)) {
                log.warn("Tenant mismatch for user '{}': token={}, header={}",
                        user.getEmail(), user.getTenantId(), tenantId);
                rejectWith(response, HttpServletResponse.SC_FORBIDDEN,
                        "Tenant ID in request does not match your account tenant");
                return;
            }
        }

        TenantContext.setTenant(tenantId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    private void rejectWith(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(
                "{\"error\":\"" + (status == 400 ? "Bad Request" : "Forbidden") + "\","
                        + "\"message\":\"" + message + "\","
                        + "\"statusCode\":" + status + "}"
        );
    }
}