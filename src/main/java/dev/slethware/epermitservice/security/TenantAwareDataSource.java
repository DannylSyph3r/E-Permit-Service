package dev.slethware.epermitservice.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.DelegatingDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

// Wraps DataSource to inject the current tenant into the PostgreSQL session before each connection reaches Hibernate.
// Drives RLS policy via SET app.current_tenant, filtering protected tables to the active tenant.
@Slf4j
public class TenantAwareDataSource extends DelegatingDataSource {

    public TenantAwareDataSource(DataSource delegate) {
        super(delegate);
    }

    @Override
    public Connection getConnection() throws SQLException {
        Connection connection = super.getConnection();
        applyTenantContext(connection);
        return connection;
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        Connection connection = super.getConnection(username, password);
        applyTenantContext(connection);
        return connection;
    }

    private void applyTenantContext(Connection connection) throws SQLException {
        String tenant = TenantContext.getTenant();
        String value = (tenant != null && !tenant.isBlank()) ? tenant : "";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute("SET app.current_tenant = '" + value + "'");
        } catch (SQLException e) {
            log.error("Failed to set tenant context on connection: {}", e.getMessage());
            throw e;
        }
    }
}