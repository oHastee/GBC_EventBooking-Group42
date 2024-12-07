package com.example.eventservice.dto;

public record EventApprovalDTO(
        String eventId,
        String eventName,
        long organizerId,
        long roomId,
        String eventType,
        String eventStart,
        String eventEnd,
        int expectedAttendees
) {
}
