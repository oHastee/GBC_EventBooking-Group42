package ca.gbc.bookingservice.dto;

import java.time.LocalDateTime;

public record ValidationRequest(
        long ownerId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        long roomId
) {
}
