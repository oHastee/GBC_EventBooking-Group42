package ca.gbc.approvalservice.dto;

import ca.gbc.approvalservice.model.Event;

public record ApprovalResponse(
        String approvalId,
        String eventId,
        String type,
        Event pendingObj,
        String action
) { }
