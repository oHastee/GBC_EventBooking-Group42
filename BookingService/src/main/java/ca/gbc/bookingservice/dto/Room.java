package ca.gbc.bookingservice.dto;

public record Room(
        Long id,
        String roomName, // or roomNum
        Integer capacity,
        String features
) { }