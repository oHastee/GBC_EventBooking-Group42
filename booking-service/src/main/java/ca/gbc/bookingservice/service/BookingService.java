package ca.gbc.bookingservice.service;

import ca.gbc.bookingservice.dto.*;

import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;

public interface BookingService {
    BookingResponse createBooking(BookingRequest productRequest);
    BookingResponse updateBooking(String bookingId, BookingUpdateRequest productRequest);
    String deleteBooking(String productId);
    List<BookingResponse> getAllBookings();
    List<BookingResponse> getAllBookings(long ownerId);
    boolean isAvailable(AvailabilityRequest availabilityRequest);

    boolean userHasBooking(ValidationRequest validationRequest);
    BookingResponse getBooking(String bookingId);
}
