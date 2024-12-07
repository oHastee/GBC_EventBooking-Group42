package com.example.eventservice.service;

import com.example.eventservice.dto.EventCreateRequest;
import com.example.eventservice.dto.EventUpdateRequest;
import com.example.eventservice.dto.EventRequestUpdateApproval;
import com.example.eventservice.dto.EventResponse;

import java.util.List;

public interface EventService {
    List<EventResponse> getAllEvents();
    EventResponse createEvent(EventCreateRequest eventRequest);
    void deleteEvent(String eventId);
    EventResponse getEventById(String eventId);

    EventResponse updateEvent(String eventId, EventUpdateRequest eventRequest);
    EventResponse updateEvent(String eventId, EventUpdateRequest eventRequest, Boolean approvalState);
    EventResponse updateApprovedEvent(String eventId, EventRequestUpdateApproval eventRequest, boolean approvalState);
    EventResponse createApprovedEvent(String eventId, boolean approvalState);
}
