package ca.gbc.bookingservice.event;

import ca.gbc.bookingservice.dto.BookingRequest.UserDetail;
import ca.gbc.bookingservice.dto.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingConfirmedEvent {

    long roomId;
    String roomName;
    String userName;
    String email;
    LocalDateTime startTime;

}