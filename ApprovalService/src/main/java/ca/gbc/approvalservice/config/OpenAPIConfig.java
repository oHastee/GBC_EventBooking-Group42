package ca.gbc.approvalservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;

public class OpenAPIConfig {
    @Bean
    public OpenAPI approvalServiceAPI() {
        return new OpenAPI()
                .info(new Info().title("Approval Service API")
                        .description("This is the REST API for Approval Service"));
    }

}
