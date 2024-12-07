package ca.gbc.bookingservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(value = "booking")
public class Booking {
    @Id
    public String id;
    public long startTime;
    public long endTime;
    public long ownerId;
    public long roomId;
    public long createdAt;

    public String purpose;
}