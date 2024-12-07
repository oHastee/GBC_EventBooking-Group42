package ca.gbc.roomservice.dto;

public record RoomResponse(
        Long id,
        String room_name, // or roomNum
        Integer capacity,
        String features
        //Boolean available
) { }
