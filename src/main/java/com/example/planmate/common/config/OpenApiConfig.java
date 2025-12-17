package com.example.planmate.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("PlanMate API")
                        .version("v1")
                        .description("PlanMate backend API documentation")
                        .contact(new Contact().name("PlanMate Team").email("support@example.com"))
                        .license(new License().name("Apache 2.0").url("http://springdoc.org")));
    }
}
