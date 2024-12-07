package ca.gbc.bookingservice.dto;

public record User(
        long id,
        String name,
        String email,
        String role
) {
}
