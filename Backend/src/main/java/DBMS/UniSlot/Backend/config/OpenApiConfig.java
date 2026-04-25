package DBMS.UniSlot.Backend.config;



import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenApiConfig — Configures Swagger/OpenAPI documentation.
 *
 * After startup, visit:
 *   http://localhost:8080/swagger-ui.html
 *
 * The "bearerAuth" scheme adds the Authorize button in Swagger UI
 * where you can paste your JWT and test all protected endpoints.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("University Slot Selection System API")
                        .version("1.0.0")
                        .description("""
                            REST API for the University Lecture Slot Selection System.
                            
                            **Roles:**
                            - `ADMIN` — manages departments, degrees, courses, professors, students.
                            - `STUDENT` — browses and selects lecture slots after fee payment.
                            
                            **Authentication:** Login via `POST /api/auth/login` to receive a JWT.
                            Paste it in the Authorize button above (format: just the token, no Bearer prefix in Swagger).
                            """)
                        .contact(new Contact()
                                .name("University IT Department")
                                .email("it@university.edu")))
                // Register the bearerAuth security scheme for Swagger UI
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .name("bearerAuth")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter your JWT token (without Bearer prefix)")));
    }
}
