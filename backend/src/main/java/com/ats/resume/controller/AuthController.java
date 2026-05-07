package com.ats.resume.controller;

import com.ats.resume.model.User;
import com.ats.resume.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            User savedUser = userRepository.save(user);
            return ResponseEntity.ok(Map.of("message", "User registered successfully", "id", savedUser.getId()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Registration failed: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginRequest) {
        Optional<User> user = userRepository.findByUsernameAndEmailAndPassword(
            loginRequest.getUsername(),
            loginRequest.getEmail(),
            loginRequest.getPassword()
        );

        if (user.isPresent()) {
            return ResponseEntity.ok(Map.of("success", true, "username", user.get().getUsername()));
        } else {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Invalid username, email, or password"));
        }
    }
}