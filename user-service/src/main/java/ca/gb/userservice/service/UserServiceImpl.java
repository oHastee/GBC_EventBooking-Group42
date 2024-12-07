package ca.gb.userservice.service;

import ca.gb.userservice.dto.UserRequest;
import ca.gb.userservice.model.User;
import ca.gb.userservice.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public List<UserRequest> getAllUsers() {
        log.info("Fetching all users");
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<UserRequest> getUserById(Long id) {
        log.info("Fetching user with ID: {}", id);
        return userRepository.findById(id)
                .map(this::convertToDTO);
    }

    @Override
    public UserRequest createUser(UserRequest userRequest) {
        log.info("Creating new user with email: {}", userRequest.email());
        User user = new User();
        user.setName(userRequest.name());
        user.setEmail(userRequest.email());
        user.setRole(userRequest.role());

        user = userRepository.save(user);
        return convertToDTO(user);
    }

    @Override
    public Optional<UserRequest> updateUser(Long id, UserRequest userRequest) {
        log.info("Updating user with ID: {}", id);
        return userRepository.findById(id)
                .map(user -> {
                    user.setName(userRequest.name());
                    user.setEmail(userRequest.email());
                    user.setRole(userRequest.role());
                    userRepository.save(user);
                    return convertToDTO(user);
                });
    }

    @Override
    public void deleteUser(Long id) {
        log.info("Deleting user with ID: {}", id);
        userRepository.deleteById(id);
    }

    private UserRequest convertToDTO(User user) {
        return new UserRequest(user.getId(), user.getName(), user.getEmail(), user.getRole());
    }
}
