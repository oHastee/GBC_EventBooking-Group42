package ca.gbc.roomservice.service;

import ca.gbc.roomservice.dto.RoomRequest;
import ca.gbc.roomservice.dto.RoomResponse;
import ca.gbc.roomservice.exception.RoomNotFoundException;
import ca.gbc.roomservice.model.Room;
import ca.gbc.roomservice.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
//@Transactional
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;

    @Override
    public RoomResponse createRoom(RoomRequest roomRequest) {
        log.info("Creating room {} {} {}", roomRequest.room_name(), roomRequest.capacity(), roomRequest.features());

        Assert.notNull(roomRequest.room_name(), "Room name cannot be null");
        Assert.notNull(roomRequest.capacity(), "Room capacity cannot be null");
        Assert.notNull(roomRequest.features(), "Room features cannot be null");

        Room room = Room.builder()
                .roomName(roomRequest.room_name())
                .capacity(roomRequest.capacity())
                .features(roomRequest.features())
                .build();
        roomRepository.save(room);
        return mapToRoomResponse(room);
    }

    @Override
    public List<RoomResponse> getAllRooms() {
        log.debug("Retrieving all rooms");

        List<Room> rooms = roomRepository.findAll();

        return rooms.stream().map(this::mapToRoomResponse).toList();
    }


    @Override
    public RoomResponse getRoomById(String id) {
        Room room = roomRepository.findById(Long.parseLong(id))
                .orElseThrow(() -> new RoomNotFoundException("Room with ID " + id + " not found"));
        return mapToRoomResponse(room);
    }

    @Override
    public RoomResponse updateRoom(String id, RoomRequest roomRequest) {
        log.debug("Updating room {} {} {} {}", id, roomRequest.room_name(), roomRequest.capacity(), roomRequest.features());

        Room room = roomRepository.findById(Long.parseLong(id))
                .orElseThrow(() -> new RoomNotFoundException("Room with ID " + id + " not found"));

        room.setRoomName(roomRequest.room_name());
        room.setCapacity(roomRequest.capacity());
        room.setFeatures(roomRequest.features());

        Room updatedRoom = roomRepository.save(room);
        return mapToRoomResponse(updatedRoom);
    }


    @Override
    public void deleteRoom(String id) {

        log.debug("Deleting product with id {}", id);
        roomRepository.deleteById(Long.valueOf(id));
    }

    // Helper method to convert Room entity to RoomResponse
    private RoomResponse mapToRoomResponse(Room room) {
        return new RoomResponse(
                room.getId(),
                room.getRoomName(),
                room.getCapacity(),
                room.getFeatures()
        );
    }
}
