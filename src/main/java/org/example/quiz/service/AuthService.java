package org.example.quiz.service;

import lombok.RequiredArgsConstructor;
import org.example.quiz.dto.auth.AuthResponse;
import org.example.quiz.dto.auth.LoginRequest;
import org.example.quiz.dto.auth.RegisterRequest;
import org.example.quiz.model.Role;
import org.example.quiz.model.User;
import org.example.quiz.repository.UserRepository;
import org.example.quiz.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;


    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByUsername(request.username())) {
            return new AuthResponse(null, null, null, "User already exists.");
        }

        User newUser = User.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .displayName(request.displayName())
                .role(Role.PLAYER)
                .enabled(true)
                .build();

        userRepository.save(newUser);

        UserDetails userDetails =
                userDetailsService.loadUserByUsername(newUser.getUsername());

        String token = jwtUtil.generateToken(userDetails);

        return new AuthResponse(
                token,
                newUser.getUsername(),
                newUser.getDisplayName(),
                newUser.getRole().name()
        );
    }


    public AuthResponse login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()
                )
        );

        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new RuntimeException("User not found."));

        UserDetails userDetails =
                userDetailsService.loadUserByUsername(request.username());

        String token = jwtUtil.generateToken(userDetails);

        return new AuthResponse(
                token,
                user.getUsername(),
                user.getDisplayName(),
                user.getRole().name()
        );
    }
}
