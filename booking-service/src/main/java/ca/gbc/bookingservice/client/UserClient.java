package ca.gbc.bookingservice.client;

import ca.gbc.bookingservice.dto.User;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;

@FeignClient(name = "userClient", url = "${user.api.url}")
public interface UserClient {

    Logger log = LoggerFactory.getLogger(UserClient.class);
    /*Get user by Id*/
    @RequestMapping("/{id}")
    @CircuitBreaker(name = "user", fallbackMethod = "fallbackMethod")
    @Retry(name = "user")
    public User getUserById(@PathVariable("id") Long id);
    default boolean fallbackMethod(Long id, Throwable throwable) {
        log.info("Cannot get user with id {}, failure reason: {}", id, throwable.getMessage());
        return false;
    }
}