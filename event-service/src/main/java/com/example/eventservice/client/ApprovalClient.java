package com.example.eventservice.client;

import com.example.eventservice.dto.Approval;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@FeignClient(name = "approvalClient", url = "${approval.api.url}")
public interface ApprovalClient {

    Logger log = LoggerFactory.getLogger(ApprovalClient.class);

    @RequestMapping(method = RequestMethod.GET, value = "/pending/{id}")
    @CircuitBreaker(name = "approval", fallbackMethod = "fallbackMethod")
    @Retry(name = "approval")
    List<Approval> getApprovalsByEventId(@PathVariable("id") String id);
    default List<Approval> fallbackMethod(String id, Throwable throwable) {
        // Log detailed information about the failure
        log.error("Fallback triggered for fetching approvals by eventId: {}. Reason: {}", id, throwable.getMessage());

        // Return an empty list as a fallback response to indicate no approvals available
        return List.of();
    }


    @RequestMapping(method = RequestMethod.POST, value = "")
    Approval createApproval(Approval approval);
}
