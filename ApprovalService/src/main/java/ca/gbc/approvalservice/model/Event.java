package ca.gbc.approvalservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Event {
    private String eventId;
    private String eventName;
    private Long organizerId;
    private Long roomId;
    private String eventType;
    private String eventStart;
    private String eventEnd;
    private Integer expectedAttendees;
}
