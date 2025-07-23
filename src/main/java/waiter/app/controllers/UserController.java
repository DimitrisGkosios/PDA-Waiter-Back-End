package waiter.app.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import waiter.app.dto.UpdateUserRequest;
import waiter.app.entities.User;
import waiter.app.repositories.UserRepository;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        Optional<User> user = userRepository.findById(id);
        return user.<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).body("User not found"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest updatedUser) {
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(404).body("User not found");
        }
        User user = userOptional.get();

        if (updatedUser.getFullName() != null) {
            user.setFullName(updatedUser.getFullName());
        }
        if (updatedUser.getEmail() != null) {
            user.setEmail(updatedUser.getEmail());
        }
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }
        if (updatedUser.getRole() != null) {
            user.setRole(updatedUser.getRole());
        }

        userRepository.save(user);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> {
                    userRepository.delete(user);
                    return ResponseEntity.ok("User deleted successfully");
                })
                .orElseGet(() -> ResponseEntity.status(404).body("User not found"));
    }
}
