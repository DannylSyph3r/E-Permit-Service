package dev.slethware.epermitservice.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.slethware.epermitservice.security.TenantAwareDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    // The primary DataSource wraps HikariCP with tenant-context injection.
    // Flyway bypasses this bean entirely to connect directly as the postgres superuser.
    @Bean
    @Primary
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("org.postgresql.Driver");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30_000);
        config.setIdleTimeout(300_000);
        config.setMaxLifetime(600_000);
        config.setConnectionTestQuery("SELECT 1");
        config.setInitializationFailTimeout(-1);

        return new TenantAwareDataSource(new HikariDataSource(config));
    }
}