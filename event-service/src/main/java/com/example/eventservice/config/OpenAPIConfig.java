package com.example.eventservice.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class OpenAPIConfig {
    @Bean
    public OpenAPI eventServiceAPI() {
        return new OpenAPI()
                .info(new Info().title("Event Service API")
                        .description("This is the REST API for Event Service"));
    }

}
