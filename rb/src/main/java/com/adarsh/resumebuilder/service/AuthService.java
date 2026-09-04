package com.adarsh.resumebuilder.service;

import com.adarsh.resumebuilder.dto.AuthDtos.AuthResponse;
import com.adarsh.resumebuilder.dto.AuthDtos.LoginRequest;
import com.adarsh.resumebuilder.dto.AuthDtos.RegisterRequest;
import com.adarsh.resumebuilder.model.User;
import com.adarsh.resumebuilder.repository.UserRepository;
import com.adarsh.resumebuilder.security.JwtService;
import com.adarsh.resumebuilder.security.MongoUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final MongoUserDetailsService userDetailsService;
    private final JwtService jwtService;

    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }
        User user = User.builder()
                .name(req.name())
                .email(req.email())
                .password(passwordEncoder.encode(req.password()))
                .build();
        userRepository.save(user);
        String token = jwtService.generateToken(userDetailsService.loadUserByUsername(user.getEmail()));
        return new AuthResponse(token, user.getName(), user.getEmail());
    }

    public AuthResponse login(LoginRequest req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password()));
        User user = userRepository.findByEmail(req.email()).orElseThrow();
        String token = jwtService.generateToken(userDetailsService.loadUserByUsername(user.getEmail()));
        return new AuthResponse(token, user.getName(), user.getEmail());
    }
}
