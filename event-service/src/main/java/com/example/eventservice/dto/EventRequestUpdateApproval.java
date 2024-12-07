package com.example.eventservice.dto;

public record EventRequestUpdateApproval(
        String eventId,
        String eventName,
        Long roomId,
        String eventType,
        String eventStart,
        String eventEnd,
        Integer expectedAttendees
) { }
