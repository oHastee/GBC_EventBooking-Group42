package ca.gbc.bookingservice.client;

import ca.gbc.bookingservice.dto.Room;
import ca.gbc.bookingservice.dto.User;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient(name = "roomClient", url = "${room.api.url}")
public interface RoomClient {

    Logger log = LoggerFactory.getLogger(RoomClient.class);

    /*Get room by Id*/
    @RequestMapping("/{id}")
    @CircuitBreaker(name = "room", fallbackMethod = "fallbackMethod")
    @Retry(name = "room")
    public Room getRoomById(@PathVariable("id") Long id);

    default boolean fallbackMethod(Long id, Throwable throwable) {
        log.info("Cannot get room with id {}, failure reason: {}", id, throwable.getMessage());
        return false;
    }
}
