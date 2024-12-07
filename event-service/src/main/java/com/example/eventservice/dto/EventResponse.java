package com.example.eventservice.dto;

import lombok.Builder;

@Builder
public record EventResponse(
        String eventId,
        String state,
        String hasPendingEdit,
        String eventName,
        long organizerId,
        long roomId,
        String eventType,
        String eventStart,
        String eventEnd,
        int expectedAttendees
) { }
