package com.teneocast.admin.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8084}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("TeneoCast Admin Service API")
                        .description("""
                            Admin Service API for TeneoCast platform management.
                            
                            This service provides:
                            - Admin user management (ROOT/OPERATOR roles)
                            - Platform settings configuration
                            - User impersonation management
                            - Cross-tenant analytics and reporting
                            
                            ## Authentication
                            All endpoints require JWT authentication with ROOT or OPERATOR role.
                            Include the JWT token in the Authorization header: `Bearer <token>`
                            
                            ## Roles
                            - **ROOT**: Full administrative access to all operations
                            - **OPERATOR**: Limited access for platform operations
                            """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("TeneoCast Team")
                                .email("admin@teneocast.com")
                                .url("https://www.teneocast.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Local Development"),
                        new Server()
                                .url("https://admin.teneocast.com")
                                .description("Production")
                ))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT token for authentication")));
    }
}
