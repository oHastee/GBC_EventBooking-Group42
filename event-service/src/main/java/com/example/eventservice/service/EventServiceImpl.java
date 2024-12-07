package com.example.eventservice.service;

import com.example.eventservice.client.ApprovalClient;
import com.example.eventservice.client.BookingClient;
import com.example.eventservice.dto.*;
import com.example.eventservice.exception.NotFoundException;
import com.example.eventservice.exception.ValidationException;
import com.example.eventservice.model.Event;
import com.example.eventservice.repository.EventRepository;
import com.mongodb.lang.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService{

    private final EventRepository eventRepository;
    private final BookingClient bookingClient;
    private final MongoTemplate mongoTemplate;

    private final ApprovalClient approvalClient;

    @Override
    public List<EventResponse> getAllEvents() {
        List<EventResponse> eventResponses = new ArrayList<>();
        List<Event> events = eventRepository.findAll();
        for (Event event:events) {
            eventResponses.add(mapToEventResponse(event))      ;
        }
        return eventResponses;
    }

    private String checkEventPending(String eventId) {
        var approval = approvalClient.getApprovalsByEventId(eventId);
        if (approval != null && !approval.isEmpty()) {
            return "T";
        }
        return "F";
    }

    private EventResponse mapToEventResponse(Event event) {

        var hasPendingEdit = checkEventPending(event.getEventId());

        var resp=  new EventResponse(event.getEventId(), event.getState(), hasPendingEdit,
                event.getEventName(), event.getOrganizerId(), event.getRoomId(), event.getEventType(),
                event.getEventStart(), event.getEventEnd(), event.getExpectedAttendees());

        if(Objects.equals(resp.state(), "planned")) {
            var start = LocalDateTime.parse(resp.eventStart());
            var end = LocalDateTime.parse(resp.eventEnd());

            if(LocalDateTime.now().isAfter(end)) {
                resp = new EventResponse(event.getEventId(), "completed", event.getHasPendingEdit(),
                        event.getEventName(), event.getOrganizerId(), event.getRoomId(), event.getEventType(),
                        event.getEventStart(), event.getEventEnd(), event.getExpectedAttendees());
            }
            else if(LocalDateTime.now().isAfter(start)) {
                resp = new EventResponse(event.getEventId(), "ongoing", event.getHasPendingEdit(),
                        event.getEventName(), event.getOrganizerId(), event.getRoomId(), event.getEventType(),
                        event.getEventStart(), event.getEventEnd(), event.getExpectedAttendees());
            }
        }

        return resp;
    }

    @Override
    public EventResponse createEvent(EventCreateRequest eventRequest) {

        Assert.notNull(eventRequest, "EventRequest cannot be null");
        Assert.hasLength(eventRequest.eventName(), "EventName cannot be empty");
        Assert.isTrue(eventRequest.organizerId() > 0, "OrganizerId cannot be 0 or negative");
        Assert.isTrue(eventRequest.roomId() > 0, "RoomId cannot be 0 or negative");
        Assert.hasLength(eventRequest.eventType(), "EventType cannot be empty");
        Assert.notNull(eventRequest.eventStart(), "EventStart cannot be null");
        Assert.notNull(eventRequest.eventEnd(), "EventEnd cannot be null");
        Assert.isTrue(eventRequest.expectedAttendees() > 0, "ExpectedAttendees cannot be 0 or negative");
        ensureStartBeforeEnd(eventRequest.eventStart(), eventRequest.eventEnd());
        ensureHasBooking(eventRequest.organizerId(), eventRequest.roomId(), eventRequest.eventStart(), eventRequest.eventEnd());
        ensureNoConflicts(eventRequest.eventStart(), eventRequest.eventEnd(), eventRequest.roomId(), null);

        Event event = Event.builder()
                .eventName(eventRequest.eventName())
                .state("pending")
                .hasPendingEdit("F")
                .organizerId(eventRequest.organizerId())
                .eventType(eventRequest.eventType())
                .roomId(eventRequest.roomId())
                .eventStart(eventRequest.eventStart().toString())
                .eventEnd(eventRequest.eventEnd().toString())
                .expectedAttendees(eventRequest.expectedAttendees())
                .build();

        event = eventRepository.save(event);

        var  approval = Approval.builder()
                .eventId(event.getEventId())
                .type("create")
                .pendingObj(event)
                .action("pending")
                .build();

        approvalClient.createApproval(approval);

        return mapToEventResponse(event);
    }

    @Override
    public EventResponse updateEvent(String eventId, EventUpdateRequest eventRequest) {
        return updateEvent(eventId, eventRequest, null);
    }

    @Override
    public EventResponse updateEvent(String eventId, EventUpdateRequest eventRequest, Boolean approvalState) {

        log.info("Updating event with id {}", eventId);
        log.info("EventRequest: RoomId: {}, EventType: {}, EventStart: {}, EventEnd: {}, ExpectedAttendees: {}",
                eventRequest.getRoomId(), eventRequest.getEventType(), eventRequest.getEventStart(), eventRequest.getEventEnd(), eventRequest.getExpectedAttendee());
        Assert.notNull(eventRequest, "EventRequest cannot be null");
        Assert.notNull(eventRequest.getUserId(), "UserId cannot be null");
        Assert.notNull(eventId, "EventId cannot be null");
        ensureEventExists(eventId);
        if(approvalState == null) {
            ensureNotPending(eventId);
        }
        ensurePermissionToUpdate(eventId, eventRequest.getUserId());
        if(eventRequest.getEventName() != null) {
            Assert.hasLength(eventRequest.getEventName(), "EventName cannot be empty");
        }

        if(eventRequest.getEventStart() != null && eventRequest.getEventEnd() != null) {
            ensureStartBeforeEnd(eventRequest.getEventStart(), eventRequest.getEventEnd());
        } else {
             if(eventRequest.getEventStart() == null) {
                 eventRequest.setEventStart(LocalDateTime.parse(getEventById(eventId).eventStart()));
             }
             if(eventRequest.getEventEnd() == null) {
                eventRequest.setEventEnd(LocalDateTime.parse(getEventById(eventId).eventEnd()));
             }
        }
        if(eventRequest.getRoomId() != null) {
            Assert.isTrue(eventRequest.getRoomId() > 0, "RoomId cannot be negative");
            if(!isOrgnaizer(eventId, eventRequest.getUserId())) {
                throw new ValidationException("Only organizer can change room");
            }
            ensureHasBooking(eventRequest.getUserId(), eventRequest.getRoomId(), eventRequest.getEventStart(), eventRequest.getEventEnd());
            ensureNoConflicts(eventRequest.getEventStart(), eventRequest.getEventEnd(), eventRequest.getRoomId(), eventId);
        }
        var event = Event.builder()
                        .eventId(eventId)
                        .eventName(eventRequest.getEventName())
                        .organizerId(eventRequest.getUserId())
                        .roomId(eventRequest.getRoomId())
                        .eventType(eventRequest.getEventType())
                        .eventStart(eventRequest.getEventStart().toString())
                        .eventEnd(eventRequest.getEventEnd().toString())
                        .expectedAttendees(eventRequest.getExpectedAttendee())
                        .build();

        log.info("Event built with id {} name {} organizerId {} roomId {} eventType {} eventStart {} eventEnd {} expectedAttendees {}",
                event.getEventId(), event.getEventName(), event.getOrganizerId(), event.getRoomId(), event.getEventType(), event.getEventStart(), event.getEventEnd(), event.getExpectedAttendees());

        if(approvalState == null) {
            approvalClient.createApproval(Approval.builder()
                    .eventId(eventId)
                    .type("update")
                    .pendingObj(event)
                    .action("pending")
                    .build());

            var savedEvent = eventRepository.findById(eventId).get();
            savedEvent.setHasPendingEdit("T");
            return mapToEventResponse(eventRepository.save(savedEvent));
        }
        if(approvalState) {
            var savedEvent = eventRepository.findById(eventId).get();
            savedEvent.setHasPendingEdit("F");
            savedEvent.setState("planned");
            if(eventRequest.getEventName() != null) {
                savedEvent.setEventName(eventRequest.getEventName());
            }
            if(eventRequest.getRoomId() != null) {
                savedEvent.setRoomId(eventRequest.getRoomId());
            }
            if(eventRequest.getEventType() != null) {
                savedEvent.setEventType(eventRequest.getEventType());
            }
            if(eventRequest.getEventStart() != null) {
                savedEvent.setEventStart(eventRequest.getEventStart().toString());
            }
            if(eventRequest.getEventEnd() != null) {
                savedEvent.setEventEnd(eventRequest.getEventEnd().toString());
            }
            if(eventRequest.getExpectedAttendee() != null) {
                savedEvent.setExpectedAttendees(eventRequest.getExpectedAttendee());
            }
            return mapToEventResponse(eventRepository.save(savedEvent));
        } else {
            var savedEvent = eventRepository.findById(eventId).get();
            savedEvent.setHasPendingEdit("F");
            if(Objects.equals(savedEvent.getState(), "pending")) {
               eventRepository.deleteById(eventId);
               return null; // Null means deleted
            }
            return mapToEventResponse(eventRepository.save(savedEvent));
        }
    }

    @Override
    public EventResponse updateApprovedEvent(String eventId, EventRequestUpdateApproval eventRequest, boolean approvalState) {
        Assert.notNull(eventRequest, "EventRequest cannot be null");
        Assert.notNull(eventId, "EventId cannot be null");
        ensureEventExists(eventId);

        var organizerId = getEventById(eventId).organizerId();
        var updateRequest = EventUpdateRequest.builder()
                .userId(organizerId)
                .eventName(eventRequest.eventName())
                .roomId(eventRequest.roomId())
                .eventType(eventRequest.eventType())
                .eventStart(LocalDateTime.parse(eventRequest.eventStart()))
                .eventEnd(LocalDateTime.parse(eventRequest.eventEnd()))
                .expectedAttendee(eventRequest.expectedAttendees())
                .build();

        return updateEvent(eventId, updateRequest, approvalState);
    }

    @Override
    public EventResponse createApprovedEvent(String eventId, boolean approvalState){
        Assert.notNull(eventId, "EventId cannot be null");
        ensureEventExists(eventId);
        var event = eventRepository.findById(eventId).get();
        if(!event.getState().equals("pending") && approvalState) {
            return mapToEventResponse(event);
        }
        if (!event.getState().equals("pending")) {
            eventRepository.deleteById(eventId);
            return null; // They rejected the event
        }
        if(approvalState) {
            var savedEvent = eventRepository.findById(eventId).get();
            savedEvent.setHasPendingEdit("F");
            savedEvent.setState("planned");
            return mapToEventResponse(eventRepository.save(savedEvent));
        } else { // Rejected. So delete the event
            var savedEvent = eventRepository.findById(eventId).get();
            savedEvent.setHasPendingEdit("F");
            eventRepository.deleteById(eventId);
            return null; // Again, null means deleted
        }
    }


    @Override
    public void deleteEvent(String eventId) {
        ensureEventExists(eventId);
        eventRepository.deleteById(eventId);
    }

    @Override
    public EventResponse getEventById(String eventId) {
        var event = eventRepository.findById(eventId)
                .orElseThrow(() ->
                        new NotFoundException("Event with id " + eventId + " not found")
                );
        return mapToEventResponse(event);
    }





    private void ensureStartBeforeEnd(LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end) || start.isEqual(end)) {
            throw new ValidationException("Event start must be before end");
        }
    }

    private void ensureHasBooking(long organizerId, long roomId, LocalDateTime start, LocalDateTime end) {
        if (!bookingClient.userHasBooking(organizerId, roomId, start, end)) {
            throw new ValidationException("User " + organizerId+ " does not have a booking for this room at this time");
        }
    }

    private void ensureNoConflicts(LocalDateTime start, LocalDateTime end, long roomId, @Nullable String ignoreEventId) {
        eventRepository.findAllByRoomId(roomId).forEach(event -> {
            LocalDateTime eventStart = LocalDateTime.parse(event.getEventStart());
            LocalDateTime eventEnd = LocalDateTime.parse(event.getEventEnd());
            if ((start.isAfter(eventStart) && start.isBefore(eventEnd)) ||
                    (end.isAfter(eventStart) && end.isBefore(eventEnd)) ||
                    (start.isBefore(eventStart) && end.isAfter(eventEnd)) ||
                    (start.isEqual(eventStart) || end.isEqual(eventEnd))) {
                if(!event.getEventId().equals(ignoreEventId)) {
                    throw new ValidationException("This event conflicts with another event in room " + roomId);
                }
            }
        });
    }

    private void ensureEventExists(String eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new NotFoundException("Event with id " + eventId + " does not exist");
        }
    }

    private void ensureNotPending(String eventId) {
        var event = eventRepository.findById(eventId).get();
        if(event.getHasPendingEdit().equals("T") || event.getState().equals("pending")) {
            throw new ValidationException("Event is already waiting for an approval");
        }
    }

    private void ensurePermissionToUpdate(String eventId, long userId) {
        if(!isOrgnaizer(eventId, userId)) {
            //TODO: Check if is staff
            throw new ValidationException("Only organizer can update the event");
        }
    }

    private boolean isOrgnaizer(String eventId, long userId) {
        var event = eventRepository.findById(eventId);
        if(event.isPresent()) {
            return event.get().getOrganizerId() == userId;
        }
        throw new NotFoundException("Event with id " + eventId + " does not exist");
    }

}
