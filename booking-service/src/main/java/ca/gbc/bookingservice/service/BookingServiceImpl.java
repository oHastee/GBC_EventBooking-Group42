package ca.gbc.bookingservice.service;

import ca.gbc.bookingservice.client.RoomClient;
import ca.gbc.bookingservice.client.UserClient;
import ca.gbc.bookingservice.dto.*;
import ca.gbc.bookingservice.event.BookingConfirmedEvent;
import ca.gbc.bookingservice.exception.NotFoundException;
import ca.gbc.bookingservice.exception.RoomNotAvailableException;
import ca.gbc.bookingservice.exception.ValidationException;
import ca.gbc.bookingservice.model.Booking;
import ca.gbc.bookingservice.repository.BookingRepository;
import com.mongodb.lang.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService{
    private final BookingRepository bookingRepository;
    private final UserClient userClient;
    private final RoomClient roomClient;

    private final KafkaTemplate<String, BookingConfirmedEvent> kafkaTemplate;

    @Value("${spring.kafka.template.default-topic}")
    private String bookingConfirmedTopic;

    @Override
    public BookingResponse createBooking(BookingRequest bookingRequest) {
        log.info("Creating booking for room {}", bookingRequest.roomId());

        ensureUserExists(bookingRequest.ownerId());
        ensureRoomExists(bookingRequest.roomId());
        ensureBookingTimeIsValid(bookingRequest.startTime(), bookingRequest.endTime());
        ensureRoomIsAvailable(bookingRequest.startTime(), bookingRequest.endTime(),
                bookingRequest.roomId(), null);

        if (bookingRequest.userDetails() == null) {
            throw new ValidationException("User details are missing from the booking request.");
        }

        var booking = Booking.builder()
                .ownerId(bookingRequest.ownerId())
                .roomId(bookingRequest.roomId())
                .startTime(bookingRequest.startTime().toEpochSecond(ZoneOffset.UTC))
                .endTime(bookingRequest.endTime().toEpochSecond(ZoneOffset.UTC))
                .createdAt(OffsetDateTime.now().toEpochSecond())
                .purpose(bookingRequest.purpose())
                .build();

        bookingRepository.save(booking);

        log.info("Booking {} created", booking.getId());

        //Send message to kafka on order-placed topic
        BookingConfirmedEvent bookingConfirmedEvent = new BookingConfirmedEvent(
                booking.getRoomId(),
                bookingRequest.roomName(),
                bookingRequest.userDetails().userName(),
                bookingRequest.userDetails().email(),
                LocalDateTime.ofInstant(Instant.ofEpochSecond(booking.getStartTime()), ZoneOffset.UTC)

        );

        log.info("Start - Sending BookingConfirmedEvent: {} to Kafka topic {}", bookingConfirmedEvent, bookingConfirmedTopic);
        kafkaTemplate.send("booking-confirmed", bookingConfirmedEvent);
        log.info("Complete - Sent BookingConfirmedEvent: {} to Kafka topic {}", bookingConfirmedEvent, bookingConfirmedTopic);

        return toBookingResponse(booking);
    }

    @Override
    public BookingResponse updateBooking(String bookingId, BookingUpdateRequest bookingUpdateRequest) {

        var booking = bookingRepository.findById(bookingId);
        if(booking.isEmpty()){
            throw new NotFoundException("Booking with id " + bookingId + " does not exist");
        }
        ensureUserIsOwnerOrStaff(booking.get(), bookingUpdateRequest.userId());
        var startTime = bookingUpdateRequest.startTime() != null ?
                bookingUpdateRequest.startTime() :
                Instant.ofEpochSecond(booking.get().getStartTime()).atZone(ZoneOffset.UTC).toLocalDateTime();
        var endTime = bookingUpdateRequest.endTime() != null ?
                bookingUpdateRequest.endTime() :
                Instant.ofEpochSecond(booking.get().getEndTime()).atZone(ZoneOffset.UTC).toLocalDateTime();

        if(bookingUpdateRequest.startTime() != null || bookingUpdateRequest.endTime() != null) {
            ensureBookingTimeIsValid(startTime, endTime);
            if(bookingUpdateRequest.roomId() != null) {
                ensureRoomExists(bookingUpdateRequest.roomId());
                ensureRoomIsAvailable(startTime, endTime, bookingUpdateRequest.roomId(), bookingId);
            }
            else
                ensureRoomIsAvailable(startTime, endTime, booking.get().getRoomId(), bookingId);
        }

        booking.get().setStartTime(startTime.toEpochSecond(ZoneOffset.UTC));
        booking.get().setEndTime(endTime.toEpochSecond(ZoneOffset.UTC));
        if(bookingUpdateRequest.purpose() != null)
            booking.get().setPurpose(bookingUpdateRequest.purpose());
        if(bookingUpdateRequest.roomId() != null)
            booking.get().setRoomId(bookingUpdateRequest.roomId());

        return toBookingResponse(bookingRepository.save(booking.get()));
    }

    @Override
    public String deleteBooking(String bookingId) {
        var booking = bookingRepository.findById(bookingId);
        if(booking.isEmpty()){
            throw new NotFoundException("Booking with id " + bookingId + " does not exist");
        }
        bookingRepository.deleteById(bookingId);
        return bookingId;
    }

    @Override
    public List<BookingResponse> getAllBookings() {
        var bookings = bookingRepository.findAll();
        return bookings.stream()
                .map(this::toBookingResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingResponse> getAllBookings(long ownerId) {
        var bookings = bookingRepository.findAllByOwnerId(ownerId);
        return bookings.stream()
                .map(this::toBookingResponse)
                .collect(Collectors.toList());
    }

    @Override
    public boolean userHasBooking(ValidationRequest validationRequest) {
        try {
            ensureUserExists(validationRequest.ownerId());
            ensureRoomExists(validationRequest.roomId());
        } catch (Exception e){
            return false;
        }
        var bookings = bookingRepository.findAllByOwnerId(validationRequest.ownerId());
        if(bookings.isEmpty()){
            return false;
        }
        for (Booking booking : bookings) {
            if(booking.getStartTime() <= validationRequest.startTime().toEpochSecond(ZoneOffset.UTC) &&
                    booking.getEndTime() >= validationRequest.endTime().toEpochSecond(ZoneOffset.UTC)){
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isAvailable(AvailabilityRequest availabilityRequest) {
        return false;
    }

    @Override
    public BookingResponse getBooking(String bookingId) {
        var booking = bookingRepository.findById(bookingId);
        if(booking.isEmpty()){
            throw new NotFoundException("Booking with id " + bookingId + " does not exist");
        }
        return booking.map(this::toBookingResponse).orElse(null);
    }



    private BookingResponse toBookingResponse(Booking booking){
        return new BookingResponse(
                booking.getId(),
                booking.getOwnerId(),
                booking.getRoomId(),
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getCreatedAt(),
                booking.getPurpose()
        );
    }

    private void ensureBookingTimeIsValid(LocalDateTime startTime, LocalDateTime endTime) {
        if(startTime.isAfter(endTime)){
            throw new ValidationException("Start time cannot be after end time");
        }
        if(startTime.isBefore(LocalDateTime.now())){
            throw new ValidationException("Start time cannot be in the past");
        }
        if(endTime.isBefore(LocalDateTime.now())){
            throw new ValidationException("End time cannot be in the past");
        }
        if(startTime.isEqual(endTime)){
            throw new ValidationException("Start time cannot be equal to end time");
        }
    }

    private void ensureRoomIsAvailable(LocalDateTime startTime, LocalDateTime endTime,
                                       long roomId, @Nullable String ignoredBookingId) {
        var bookings = bookingRepository.findAllByRoomId(roomId);
        for (Booking booking : bookings) {
            if((booking.getStartTime() <= startTime.toEpochSecond(ZoneOffset.UTC) &&
                    booking.getEndTime() >= startTime.toEpochSecond(ZoneOffset.UTC))
                    || (booking.getStartTime() <= endTime.toEpochSecond(ZoneOffset.UTC) &&
                    booking.getEndTime() >= endTime.toEpochSecond(ZoneOffset.UTC))
            || (booking.getStartTime() >= startTime.toEpochSecond(ZoneOffset.UTC) &&
                    booking.getEndTime() <= endTime.toEpochSecond(ZoneOffset.UTC))){
                if(!booking.getId().equals(ignoredBookingId)){
                    throw new RoomNotAvailableException("Room " + roomId + " is not available for the requested time slot");
                }
            }
        }
    }

    private void ensureUserIsOwnerOrStaff(Booking booking, long userId) {
        if(booking.getOwnerId() != userId){
            if(checkIfUserIsStaff(userId)){
                return;
            }
            throw new ValidationException("User " + userId + " is not authorized to update booking " + booking.getId());
        }
    }
    private void ensureUserExists(long ownerId) {
        var user = userClient.getUserById(ownerId);
        if(user == null){
            throw new NotFoundException("User with id " + ownerId + " does not exist");
        }
    }
    private void ensureRoomExists(long roomId) {
        var room = roomClient.getRoomById(roomId);
        if(room == null){
            throw new NotFoundException("Room with id " + roomId + " does not exist");
        }
    }

    private boolean checkIfUserIsStaff(long userId) {
        var user = userClient.getUserById(userId);
        return user.role().equals("staff");
    }
}
