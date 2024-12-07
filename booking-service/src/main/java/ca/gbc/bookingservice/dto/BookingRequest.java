package ca.gbc.bookingservice.dto;

import java.time.LocalDateTime;

public record BookingRequest(
        long ownerId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        long roomId,
        String purpose,

        // added for Kafka
        String roomName,
        UserDetail userDetails

) {
    public record UserDetail( String userName, String email){}
}