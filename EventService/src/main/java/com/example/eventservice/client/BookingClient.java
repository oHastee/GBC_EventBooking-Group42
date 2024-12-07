package com.example.eventservice.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

@FeignClient(name = "bookingClient", url = "${booking.api.url}")
public interface BookingClient {

    Logger log = LoggerFactory.getLogger(BookingClient.class);

    @RequestMapping(method = RequestMethod.GET, value = "/userHasRoomBooked")
    @CircuitBreaker(name = "booking", fallbackMethod = "fallbackMethod")
    @Retry(name = "booking")

    Boolean userHasBooking(@RequestParam("userId") Long userId,
                           @RequestParam("roomId") Long roomId,
                            @RequestParam("startTime") LocalDateTime startTime,
                            @RequestParam("endTime") LocalDateTime endTime);

    default boolean fallbackMethod(Long userId,
                                   Long roomId,
                                   LocalDateTime startTime,
                                   LocalDateTime endTime,
                                   Throwable throwable) {
        // Log detailed information about the failure
        log.error("Fallback triggered for userId: {}, roomId: {}, startTime: {}, endTime: {}. Reason: {}",
                userId, roomId, startTime, endTime, throwable.getMessage());

        // Check for specific exceptions or error messages
        if (throwable.getMessage().contains("roomId")) {
            log.warn("Issue might be related to roomId: {}", roomId);
        } else if (throwable.getMessage().contains("userId")) {
            log.warn("Issue might be related to userId: {}", userId);
        }

        // Return a default value indicating failure
        return false;
    }

}
