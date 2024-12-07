package com.example.eventservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(value = "Event")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Event {
    @Id
    private String eventId;
    private String state;
    private String hasPendingEdit;
    private String eventName;
    private Long organizerId;
    private Long roomId;
    private String eventType;
    private String eventStart;
    private String eventEnd;
    private Integer expectedAttendees;
}
