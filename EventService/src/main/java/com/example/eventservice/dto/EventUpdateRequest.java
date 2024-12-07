package com.example.eventservice.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class EventUpdateRequest {
    public String eventName;
    public Long userId;
    public Long roomId;
    public String eventType;
    public LocalDateTime eventStart;
    public LocalDateTime eventEnd;
    public Integer expectedAttendee;
}
