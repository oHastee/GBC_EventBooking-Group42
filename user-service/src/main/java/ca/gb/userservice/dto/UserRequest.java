package ca.gb.userservice.dto;

public record UserRequest(
        Long id,
        String name,
        String email,
        String role
){}
