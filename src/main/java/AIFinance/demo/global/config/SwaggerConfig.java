package AIFinance.demo.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    private static final String JWT_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI openAPI() {
        SecurityRequirement securityRequirement =
                new SecurityRequirement()
                        .addList(JWT_SCHEME_NAME);

        SecurityScheme securityScheme =
                new SecurityScheme()
                        .name(JWT_SCHEME_NAME)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT");

        Components components =
                new Components()
                        .addSecuritySchemes(
                                JWT_SCHEME_NAME,
                                securityScheme
                        );

        return new OpenAPI()
                .info(
                        new Info()
                                .title("AI Finance API 명세서")
                                .description(
                                        "AI 영수증 기반 여행 정산 서비스 API 문서입니다."
                                )
                                .version("v1.0.0")
                )
                .addSecurityItem(securityRequirement)
                .components(components);
    }
}