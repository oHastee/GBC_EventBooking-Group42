package ca.gbc.roomservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;

public class OpenAPIConfig {
    @Bean
    public OpenAPI roomServiceAPI() {
        return new OpenAPI()
                .info(new Info().title("Room Service API")
                        .description("This is the REST API for Room Service"));
    }

}
