package ca.gbc.approvalservice.dto;

public record User(
        long id,
        String name,
        String email,
        String role
) {
}
