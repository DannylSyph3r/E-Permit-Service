package dev.slethware.epermitservice;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@OpenAPIDefinition(
		info = @Info(
				contact = @Contact(name = "Slethware", email = "info@slethware.dev"),
				description = "Multi-Tenant E-Permit Service API",
				title = "E-Permit Service API Documentation",
				version = "1.0"
		),
		security = {
				@SecurityRequirement(name = "bearerAuth"),
				@SecurityRequirement(name = "X-Tenant-ID")
		}
)
@SecuritySchemes({
		@SecurityScheme(
				name = "bearerAuth",
				description = "JWT — paste the token returned by POST /api/auth/token",
				scheme = "bearer",
				type = SecuritySchemeType.HTTP,
				bearerFormat = "JWT",
				in = SecuritySchemeIn.HEADER
		),
		@SecurityScheme(
				name = "X-Tenant-ID",
				description = "Active tenant — Ministry_Health or Ministry_Education",
				type = SecuritySchemeType.APIKEY,
				in = SecuritySchemeIn.HEADER,
				paramName = "X-Tenant-ID"
		)
})
@EnableJpaAuditing
@SpringBootApplication
public class EPermitServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(EPermitServiceApplication.class, args);
	}
}