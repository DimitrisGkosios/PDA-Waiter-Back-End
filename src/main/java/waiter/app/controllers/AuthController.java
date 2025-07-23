package waiter.app.controllers;

import waiter.app.dto.RegisterRequest;
import waiter.app.dto.AuthRequest;
import waiter.app.dto.AuthResponse;
import waiter.app.entities.User;
import waiter.app.Enums.Role;
import waiter.app.repositories.UserRepository;
import waiter.app.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepo;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            // Παίρνουμε το πρώτο μήνυμα λάθους
            String errorMessage = bindingResult.getFieldError().getDefaultMessage();
            return ResponseEntity.badRequest().body(errorMessage);
        }

        if (userRepo.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email already taken");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setFullName(request.getFullName());

        userRepo.save(user);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }

        User user = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        return ResponseEntity.ok(new AuthResponse(token));
    }
}
