package ca.gbc.bookingservice.controller;

import ca.gbc.bookingservice.dto.*;
import ca.gbc.bookingservice.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/booking")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<BookingResponse> createBooking(@RequestBody BookingRequest bookingRequest) {
        var bookingResponse = bookingService.createBooking(bookingRequest);
        return new ResponseEntity<>(bookingResponse, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<BookingResponse>> getAllBookings(@RequestParam(required = false) Long userId) {
        if(userId != null) {
            List<BookingResponse> bookingResponses = bookingService.getAllBookings(userId);
            return ResponseEntity.ok(bookingResponses);
        }
        List<BookingResponse> bookingResponses = bookingService.getAllBookings();
        return ResponseEntity.ok(bookingResponses);
    }

    @GetMapping("/userHasRoomBooked")
    public ResponseEntity<Boolean> userHasBooking(
            @RequestParam long userId,
            @RequestParam long roomId,
            @RequestParam LocalDateTime startTime,
            @RequestParam LocalDateTime endTime) {
        if(userId == 0 || roomId == 0 || startTime == null || endTime == null)
            return ResponseEntity.badRequest().build();
        var availabilityRequest = new ValidationRequest(userId, startTime, endTime, roomId);
        return ResponseEntity.ok(bookingService.userHasBooking(availabilityRequest));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable String id) {
        var bookingResponse = bookingService.getBooking(id);
        if(bookingResponse == null)
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(bookingResponse);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBooking(@PathVariable String id) {
        return ResponseEntity.ok(bookingService.deleteBooking(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookingResponse> updateBooking(@PathVariable String id, @RequestBody BookingUpdateRequest bookingUpdateRequest) {
        if(bookingService.getBooking(id) == null)
            return ResponseEntity.notFound().build();
        var response = bookingService.updateBooking(id, bookingUpdateRequest);
        if(response == null)
            return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(response);
    }


}
