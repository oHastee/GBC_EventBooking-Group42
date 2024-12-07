package ca.gb.userservice.service;

import ca.gb.userservice.dto.UserRequest;
import java.util.List;
import java.util.Optional;

public interface UserService {
    List<UserRequest> getAllUsers();
    Optional<UserRequest> getUserById(Long id);
    UserRequest createUser(UserRequest userRequest);
    Optional<UserRequest> updateUser(Long id, UserRequest userRequest);
    void deleteUser(Long id);
}
