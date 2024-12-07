package ca.gbc.apigateway.routes;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.server.mvc.filter.CircuitBreakerFilterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import java.net.URI;

import static org.springframework.cloud.gateway.server.mvc.filter.FilterFunctions.setPath;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;

@Configuration
@Slf4j
public class Routes {

    @Value("${services.user.url}")
    private String userServiceUrl;

    @Value("${services.room.url}")
    private String roomServiceUrl;

    @Value("${services.booking.url}")
    private String bookingServiceUrl;

    @Value("${services.event.url}")
    private String eventServiceUrl;

    @Value("${services.approval.url}")
    private String approvalServiceUrl;

    // User Service Route
    @Bean
    public RouterFunction<ServerResponse> userServiceRoute() {
        return GatewayRouterFunctions.route("user_service")
                .route(RequestPredicates.path("/api/users"), request -> {
                    log.info("Received request for user service {}", request.uri());
                    return HandlerFunctions.http(userServiceUrl).handle(request);

                })
                .filter(CircuitBreakerFilterFunctions
                        .circuitBreaker("userServiceCircuitBreaker", URI.create("forward:/fallbackRoute")))
                .build();
    }

    // Room Service Route
    @Bean
    public RouterFunction<ServerResponse> roomServiceRoute() {
        return GatewayRouterFunctions.route("room_service")
                .route(RequestPredicates.path("/api/room"), request -> {
                    log.info("Received request for room service {}", request.uri());
                    return HandlerFunctions.http(roomServiceUrl).handle(request);

                })
                .filter(CircuitBreakerFilterFunctions
                        .circuitBreaker("roomServiceCircuitBreaker", URI.create("forward:/fallbackRoute")))
                .build();
    }

    // Booking Service Route
    @Bean
    public RouterFunction<ServerResponse> bookingServiceRoute() {
        return GatewayRouterFunctions.route("booking_service")
                .route(RequestPredicates.path("/api/booking"), request -> {
                    log.info("Received request for booking service {}", request.uri());
                    return HandlerFunctions.http(bookingServiceUrl).handle(request);

                })
                .filter(CircuitBreakerFilterFunctions
                        .circuitBreaker("bookingServiceCircuitBreaker", URI.create("forward:/fallbackRoute")))
                .build();
    }

    // Event Service Route
    @Bean
    public RouterFunction<ServerResponse> eventServiceRoute() {
        return GatewayRouterFunctions.route("event_service")
                .route(RequestPredicates.path("/api/event"), request -> {
                    log.info("Received request for event service {}", request.uri());
                    return HandlerFunctions.http(eventServiceUrl).handle(request);

                })
                .filter(CircuitBreakerFilterFunctions
                        .circuitBreaker("eventServiceCircuitBreaker", URI.create("forward:/fallbackRoute")))
                .build();
    }

    // Approval Service Route
    @Bean
    public RouterFunction<ServerResponse> approvalServiceRoute() {
        return GatewayRouterFunctions.route("approval_service")
                .route(RequestPredicates.path("/api/approval"), request -> {
                    log.info("Received request for approval service {}", request.uri());
                    return HandlerFunctions.http(approvalServiceUrl).handle(request);

                })
                .filter(CircuitBreakerFilterFunctions
                        .circuitBreaker("approvalServiceCircuitBreaker", URI.create("forward:/fallbackRoute")))
                .build();
    }

    // Swagger Aggregation Routes
    @Bean
    public RouterFunction<ServerResponse> userServiceSwagger() {
        return GatewayRouterFunctions.route("user_service_swagger")
                .route(RequestPredicates.path("/aggregate/user_service/api-docs"),
                        HandlerFunctions.http(userServiceUrl))
                .filter(setPath("/api-docs"))
                .filter(CircuitBreakerFilterFunctions
                        .circuitBreaker("UserSwaggerCircuitBreaker", URI.create("forward:/fallbackRoute")))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> roomServiceSwagger() {
        return GatewayRouterFunctions.route("room_service_swagger")
                .route(RequestPredicates.path("/aggregate/room_service/api-docs"),
                        HandlerFunctions.http(roomServiceUrl))
                .filter(setPath("/api-docs"))
                .filter(CircuitBreakerFilterFunctions
                        .circuitBreaker("RoomSwaggerCircuitBreaker", URI.create("forward:/fallbackRoute")))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> bookingServiceSwagger() {
        return GatewayRouterFunctions.route("booking_service_swagger")
                .route(RequestPredicates.path("/aggregate/booking_service/api-docs"),
                        HandlerFunctions.http(bookingServiceUrl))
                .filter(setPath("/api-docs"))
                .filter(CircuitBreakerFilterFunctions
                        .circuitBreaker("BookingSwaggerCircuitBreaker", URI.create("forward:/fallbackRoute")))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> eventServiceSwagger() {
        return GatewayRouterFunctions.route("event_service_swagger")
                .route(RequestPredicates.path("/aggregate/event_service/api-docs"),
                        HandlerFunctions.http(eventServiceUrl))
                .filter(setPath("/api-docs"))
                .filter(CircuitBreakerFilterFunctions
                        .circuitBreaker("BookingSwaggerCircuitBreaker", URI.create("forward:/fallbackRoute")))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> approvalServiceSwagger() {
        return GatewayRouterFunctions.route("approval_service_swagger")
                .route(RequestPredicates.path("/aggregate/approval_service/api-docs"),
                        HandlerFunctions.http(approvalServiceUrl))
                .filter(setPath("/api-docs"))
                .filter(CircuitBreakerFilterFunctions
                        .circuitBreaker("ApprovalSwaggerCircuitBreaker", URI.create("forward:/fallbackRoute")))
                .build();
    }

    // Fallback Route
    @Bean
    public RouterFunction<ServerResponse> fallbackRoute(){

        return route("fallbackRoute")
                .route(RequestPredicates.all(),
                        request ->ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE)
                                .body("Service is Temporarily Unavailable, please try again later"))
                .build();
    }
}
