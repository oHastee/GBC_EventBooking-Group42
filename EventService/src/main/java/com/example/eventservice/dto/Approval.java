package com.example.eventservice.dto;

import com.example.eventservice.model.Event;
import lombok.Builder;

@Builder
public record Approval(
        String eventId,
        String type,
        Event pendingObj,
        String action
) { }
