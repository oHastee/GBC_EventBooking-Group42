package ca.gbc.bookingservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;

public class OpenAPIConfig {
    @Bean
    public OpenAPI bookingServiceAPI() {
        return new OpenAPI()
                .info(new Info().title("Booking Service API")
                        .description("This is the REST API for Booking Service"));
    }

}
