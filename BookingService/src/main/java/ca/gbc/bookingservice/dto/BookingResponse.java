package ca.gbc.bookingservice.dto;

public record BookingResponse (
    String id,
    long ownerId,
    long roomId,
    long startTime,
    long endTime,
    long createdAt,
    String purpose
){}