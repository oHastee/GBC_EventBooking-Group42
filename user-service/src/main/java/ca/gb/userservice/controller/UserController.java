package ca.gb.userservice.controller;

import ca.gb.userservice.dto.UserRequest;
import ca.gb.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    //Get - Works
    @GetMapping
    public ResponseEntity<List<UserRequest>> getAllUsers() {
        List<UserRequest> users = userService.getAllUsers();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserRequest> getUserById(@PathVariable Long id) {
        Optional<UserRequest> user = userService.getUserById(id);
        return user.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    //Create - Works
    @PostMapping
    public ResponseEntity<UserRequest> createUser(@RequestBody UserRequest userRequest) {
        System.out.println("Received user creation request: " + userRequest);
        UserRequest createdUser = userService.createUser(userRequest);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }



    //Update - Works
    @PutMapping("/{id}")
    public ResponseEntity<UserRequest> updateUser(@PathVariable Long id, @RequestBody UserRequest userRequest) {
        Optional<UserRequest> updatedUser = userService.updateUser(id, userRequest);
        return updatedUser.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }


    //Delete
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
