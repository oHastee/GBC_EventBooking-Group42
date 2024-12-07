package ca.gbc.roomservice.service;

import ca.gbc.roomservice.dto.RoomRequest;
import ca.gbc.roomservice.dto.RoomResponse;

import java.util.List;

public interface RoomService {

    RoomResponse createRoom(RoomRequest roomRequest);
    List<RoomResponse> getAllRooms();
    RoomResponse getRoomById(String id);
    RoomResponse updateRoom(String id, RoomRequest roomRequest);
    void deleteRoom(String id);
}

/*
ProductResponse createProduct(ProductRequest productRequest);
    List<ProductResponse> getAllProducts();
    String updateProduct(String id, ProductRequest productRequest);
    void deleteProduct(String id);
 */