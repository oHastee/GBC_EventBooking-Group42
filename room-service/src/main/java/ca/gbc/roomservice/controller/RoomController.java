package ca.gbc.roomservice.controller;

import ca.gbc.roomservice.dto.RoomRequest;
import ca.gbc.roomservice.dto.RoomResponse;
import ca.gbc.roomservice.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/room")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<RoomResponse> createRoom(@RequestBody RoomRequest roomRequest) {
        RoomResponse createdRoom = roomService.createRoom(roomRequest);

        HttpHeaders headers = new HttpHeaders();
        // Fix: Add a leading slash to the Location header path
        headers.add("Location", "/api/room/" + createdRoom.id());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .body(createdRoom);
    }

    @GetMapping
    public ResponseEntity<List<RoomResponse>> getAllRooms() {
        List<RoomResponse> rooms = roomService.getAllRooms();
        return ResponseEntity.status(HttpStatus.OK).body(rooms);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoomResponse> getRoomById(@PathVariable String id) {
        RoomResponse room = roomService.getRoomById(id);
        return ResponseEntity.status(HttpStatus.OK).body(room);
    }

    @PutMapping("/{roomId}")  // Fix: Path should contain roomId
    public ResponseEntity<RoomResponse> updateRoom(@PathVariable("roomId") String roomId,
                                                   @RequestBody RoomRequest roomRequest) {

        RoomResponse updatedRoom = roomService.updateRoom(roomId, roomRequest);

        HttpHeaders headers = new HttpHeaders();
        // Fix: Add a leading slash to the Location header path and return updated room
        headers.add("Location", "/api/room/" + updatedRoom.id());

        return ResponseEntity
                .status(HttpStatus.OK)
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .body(updatedRoom);  // Return updated room
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> deleteRoom(@PathVariable("roomId") String roomId) {
        roomService.deleteRoom(roomId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);  // Return No Content for delete
    }
}
