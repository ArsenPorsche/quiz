package org.example.quiz.controller;

import lombok.RequiredArgsConstructor;
import org.example.quiz.dto.PlayerDto;
import org.example.quiz.dto.auth.AuthResponse;
import org.example.quiz.dto.auth.LoginRequest;
import org.example.quiz.dto.auth.RegisterRequest;
import org.example.quiz.model.User;
import org.example.quiz.repository.UserRepository;
import org.example.quiz.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
    @GetMapping("/players")
    public ResponseEntity<List<PlayerDto>> getPlayers() {
        List<PlayerDto> players = userRepository.findAll().stream()
                .filter(User::isEnabled)
                .map(user -> new PlayerDto(
                        user.getUsername(),
                        user.getDisplayName() != null ? user.getDisplayName() : user.getUsername(),
                        user.getRole().name()
                ))
                .sorted((a, b) -> a.displayName().compareToIgnoreCase(b.displayName()))
                .toList();

        return ResponseEntity.ok(players);
    }
}