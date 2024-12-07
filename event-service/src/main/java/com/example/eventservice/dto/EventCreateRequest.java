package com.example.eventservice.dto;

import java.time.LocalDateTime;

public record EventCreateRequest(
        String eventName,
        long organizerId,
        long roomId,
        String eventType,
        LocalDateTime eventStart,
        LocalDateTime eventEnd,
        int expectedAttendees
) { }
