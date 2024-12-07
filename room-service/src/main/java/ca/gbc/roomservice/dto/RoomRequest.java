package ca.gbc.roomservice.dto;

import jakarta.validation.constraints.*;

public record RoomRequest(
        Long id, // Include only if needed for updates

        @NotBlank(message = "Room name cannot be blank")
        String room_name,

        @Min(value = 1, message = "Capacity must be greater than zero")
        Integer capacity,

        String features // Optional: can add length constraints if needed

        //@NotNull(message = "Availability must be specified")
        //Boolean available
) { }
