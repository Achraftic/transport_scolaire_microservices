package com.transport.auth_service.service;

import com.transport.auth_service.dto.AuthResponse;
import com.transport.auth_service.dto.LoginRequest;
import com.transport.auth_service.dto.RegisterRequest;
import com.transport.auth_service.entity.User;
import com.transport.auth_service.exception.AuthException;
import com.transport.auth_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new AuthException("User with email " + request.getEmail() + " already exists", HttpStatus.CONFLICT);
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setRole(request.getRole());
        user.setEnabled(true);
        userRepository.save(user);

        String token = jwtService.generateToken(user);

        return AuthResponse.builder()
                .accessToken(token)
                .username(user.getEmail())
                .roles(user.getRole().name())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        } catch (Exception e) {
            throw new AuthException("Invalid credentials", HttpStatus.UNAUTHORIZED);
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthException("User not found", HttpStatus.NOT_FOUND));

        String token = jwtService.generateToken(user);

        return AuthResponse.builder()
                .accessToken(token)
                .username(user.getEmail())
                .roles(user.getRole().name())
                .build();
    }

}
