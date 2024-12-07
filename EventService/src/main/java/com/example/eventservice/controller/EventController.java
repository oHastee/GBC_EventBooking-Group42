package com.example.eventservice.controller;

import com.example.eventservice.dto.EventCreateRequest;
import com.example.eventservice.dto.EventUpdateRequest;
import com.example.eventservice.dto.EventRequestUpdateApproval;
import com.example.eventservice.dto.EventResponse;
import com.example.eventservice.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/event")
@RequiredArgsConstructor
public class EventController {
    private final EventService eventService;

    @PostMapping
    public ResponseEntity<EventResponse> createEvent(@RequestBody EventCreateRequest eventRequest) {
        var createdEvent = eventService.createEvent(eventRequest);
        return new ResponseEntity<>(createdEvent, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<EventResponse>> getAllEvents() {
        return new ResponseEntity<>(eventService.getAllEvents(), HttpStatus.OK);
    }

    @PutMapping("/{eventId}")
    public ResponseEntity<EventResponse> updateEvent(@PathVariable String eventId, @RequestBody EventUpdateRequest eventRequest) {
        var responseEvent = eventService.updateEvent(eventId, eventRequest);
        if (responseEvent == null) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(responseEvent, HttpStatus.OK);
    }

    @PutMapping("/approve/{eventId}")
    public ResponseEntity<EventResponse> updateApprovedEvent(@PathVariable String eventId,
                                                             @RequestBody EventRequestUpdateApproval eventRequest) {

        var responseEvent = eventService.updateApprovedEvent(eventId, eventRequest, true);
        if (responseEvent == null) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(responseEvent, HttpStatus.OK);
    }

    @PutMapping("/reject/{eventId}")
    public ResponseEntity<EventResponse> updateRejectedEvent(@PathVariable String eventId,
                                                             @RequestBody EventRequestUpdateApproval eventRequest) {

        var responseEvent = eventService.updateApprovedEvent(eventId, eventRequest, false);
        if (responseEvent == null) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(responseEvent, HttpStatus.OK);
    }

    @DeleteMapping("/{eventId}")
    public ResponseEntity<EventResponse> deleteEvent(@PathVariable String eventId) {
        eventService.deleteEvent(eventId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventResponse> getEventById(@PathVariable String eventId) {
        var eventResponse = eventService.getEventById(eventId);
        return new ResponseEntity<>(eventResponse, HttpStatus.OK);
    }
}
