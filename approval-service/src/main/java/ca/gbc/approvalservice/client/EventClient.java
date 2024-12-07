package ca.gbc.approvalservice.client;

import ca.gbc.approvalservice.dto.EventRequestUpdateApproval;
import ca.gbc.approvalservice.dto.EventResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "eventClient", url = "${event.api.url}")
public interface EventClient {

    Logger log = LoggerFactory.getLogger(EventClient.class);

    @RequestMapping(method = RequestMethod.GET, value = "/{eventId}")
    @CircuitBreaker(name = "eventService", fallbackMethod = "getEventFallback")
    @Retry(name = "eventService")
    EventResponse getEvent(@PathVariable("eventId") String eventId);

    @RequestMapping(method = RequestMethod.PUT, value = "/approve/{eventId}")
    @CircuitBreaker(name = "eventService", fallbackMethod = "updateApprovedEventFallback")
    @Retry(name = "eventService")
    EventResponse updateApprovedEvent(@PathVariable("eventId") String eventId, @RequestBody EventRequestUpdateApproval eventRequestUpdateApproval);

    @RequestMapping(method = RequestMethod.PUT, value = "/reject/{eventId}")
    @CircuitBreaker(name = "eventService", fallbackMethod = "updateRejectedEventFallback")
    @Retry(name = "eventService")
    EventResponse updateRejectedEvent(@PathVariable("eventId") String eventId, @RequestBody EventRequestUpdateApproval eventRequestUpdateApproval);

    // Fallback for getEvent
    default EventResponse getEventFallback(String eventId, Throwable throwable) {
        log.error("Fallback triggered for getEvent with eventId {}: {}", eventId, throwable.getMessage());
        return EventResponse.builder()
                .eventId(eventId != null ? eventId : "unknown-event-id")
                .state("UNKNOWN")
                .hasPendingEdit("false")
                .eventName("Fallback Event")
                .organizerId(0L)
                .roomId(0L)
                .eventType("unknown")
                .eventStart("N/A")
                .eventEnd("N/A")
                .expectedAttendees(0)
                .build();
    }

    // Fallback for updateApprovedEvent
    default EventResponse updateApprovedEventFallback(String eventId, EventRequestUpdateApproval request, Throwable throwable) {
        log.error("Fallback triggered for updateApprovedEvent with eventId {}: {}", eventId, throwable.getMessage());
        return EventResponse.builder()
                .eventId(eventId)
                .state("APPROVAL_PENDING")
                .hasPendingEdit("true")
                .eventName(request != null ? "Fallback Name" : "Unknown")
                .organizerId(0L)
                .roomId(0L)
                .eventType("unknown")
                .eventStart("N/A")
                .eventEnd("N/A")
                .expectedAttendees(0)
                .build();
    }

    // Fallback for updateRejectedEvent
    default EventResponse updateRejectedEventFallback(String eventId, EventRequestUpdateApproval request, Throwable throwable) {
        log.error("Fallback triggered for updateRejectedEvent with eventId {}: {}", eventId, throwable.getMessage());
        return EventResponse.builder()
                .eventId(eventId)
                .state("REJECTION_PENDING")
                .hasPendingEdit("true")
                .eventName(request != null ? "Fallback Name" : "Unknown")
                .organizerId(0L)
                .roomId(0L)
                .eventType("unknown")
                .eventStart("N/A")
                .eventEnd("N/A")
                .expectedAttendees(0)
                .build();
    }
}



//    @RequestMapping(method = RequestMethod.PUT, value = "/approve/{eventId}")
//    EventResponse approveEvent(@PathVariable("eventId") String eventId);

//    @RequestMapping(method = RequestMethod.PUT, value = "/reject/{eventId}")
//    EventResponse approveEvent(@PathVariable("eventId") String eventId);

