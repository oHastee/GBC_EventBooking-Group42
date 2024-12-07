package ca.gbc.approvalservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(value = "Approval")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Approval {
    @Id
    private String approvalId;
    private String eventId;
    private String type;
    private Event pendingObj;
    private String action;
}