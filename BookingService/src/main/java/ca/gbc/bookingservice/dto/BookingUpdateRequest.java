package ca.gbc.bookingservice.dto;

import com.mongodb.lang.Nullable;

import java.time.LocalDateTime;

public record BookingUpdateRequest (
    long userId, // Can be owner or staff
    @Nullable LocalDateTime startTime,
    @Nullable LocalDateTime endTime,
    @Nullable Long roomId,
    @Nullable String purpose
){}