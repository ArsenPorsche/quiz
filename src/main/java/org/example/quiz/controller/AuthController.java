package org.example.quiz.controller;

import lombok.RequiredArgsConstructor;
import org.example.quiz.dto.auth.AuthResponse;
import org.example.quiz.dto.auth.LoginRequest;
import org.example.quiz.dto.auth.RegisterRequest;
import org.example.quiz.model.Role;
import org.example.quiz.model.User;
import org.example.quiz.repository.UserRepository;
import org.example.quiz.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;


    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {

        if (userRepository.existsByUsername(request.username())) {
            return ResponseEntity.badRequest()
                    .body(new AuthResponse(null, null, null, "User already exists."));
        }

        User newUser = User.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .displayName(request.displayName())
                .role(Role.PLAYER)
                .enabled(true)
                .build();

        userRepository.save(newUser);

        UserDetails userDetails = userDetailsService.loadUserByUsername(newUser.getUsername());
        String token = jwtUtil.generateToken(userDetails);

        return ResponseEntity.ok(new AuthResponse(
                token,
                newUser.getUsername(),
                newUser.getDisplayName(),
                newUser.getRole().name()
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.username());
        String token = jwtUtil.generateToken(userDetails);

        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new RuntimeException("User is not found."));

        return ResponseEntity.ok(new AuthResponse(
                token,
                user.getUsername(),
                user.getDisplayName(),
                user.getRole().name()
        ));
    }
}