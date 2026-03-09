package dev.ilkersahin.java.spring.gym.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Gym CRM API",
                version = "1.1-SNAPSHOT",
                description = """
                        REST API for Gym Customer Relationship Management system.
                        
                        ## Authentication
                        
                        This API uses **JWT (JSON Web Token)** Bearer authentication for protected endpoints.
                        
                        ### How to authenticate:
                        1. Register as a **Trainee** (`POST /api/v1/trainees`) or **Trainer** (`POST /api/v1/trainers`)
                        2. Login with your credentials (`POST /api/v1/auth/login`)
                        3. Copy the `token` from the response
                        4. Click the **Authorize** button (🔓) above
                        5. Enter: `Bearer <your-token>` (or just the token - Swagger UI adds "Bearer" prefix)
                        6. Click **Authorize** to save
                        
                        ### Public endpoints (no authentication required):
                        - `POST /api/v1/auth/login` - User login
                        - `POST /api/v1/trainees` - Register trainee
                        - `POST /api/v1/trainers` - Register trainer
                        - `GET /api/v1/training-types` - List training types
                        - `GET /health` - Health check
                        
                        ### Token details:
                        - **Format**: JWT (JSON Web Token)
                        - **Expiration**: 24 hours
                        - **Claims**: `sub` (username), `role` (TRAINEE/TRAINER), `iat`, `exp`
                        
                        """,
                contact = @Contact(name = "İlker Ata Şahin")
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Local development server")
        }
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = """
                JWT Bearer token authentication.
                
                **How to obtain a token:**
                1. Login via `POST /api/v1/auth/login`
                2. Copy the `token` from the response
                3. Click "Authorize" and enter the token
                
                **Token format:** `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...`
                
                **Token expiration:** 24 hours
                """
)
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .tags(List.of(
                        new Tag()
                                .name("Authentication")
                                .description("""
                                        Login and token management.
                                        
                                        **Flow**: Register → Login → Use token for protected endpoints → Logout
                                        """),
                        new Tag()
                                .name("Trainees")
                                .description("Trainee registration and profile management"),
                        new Tag()
                                .name("Trainers")
                                .description("Trainer registration and profile management"),
                        new Tag()
                                .name("Trainings")
                                .description("Training session management"),
                        new Tag()
                                .name("Training Types")
                                .description("Training type catalog (public)")
                ));
    }
}
