package com.is.in_studio.service;

import java.time.Instant;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import com.is.in_studio.auth.JwtUtil;
import com.is.in_studio.config.JwtProperties;
import com.is.in_studio.domain.dto.AuthResponseDto;
import com.is.in_studio.domain.dto.UserResponseDto;
import com.is.in_studio.domain.input.LoginInput;
import com.is.in_studio.domain.input.UserInput;
import com.is.in_studio.entity.User;
import com.is.in_studio.exception.CustomExceptions.ProcessServiceException;
import com.is.in_studio.repository.UserRepository;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final JwtProperties jwtProperties;
    private final EmailConfirmationService emailConfirmationService;

    public AuthService(AuthenticationManager authenticationManager, UserRepository userRepository,
                       UserService userService, JwtUtil jwtUtil, JwtProperties jwtProperties,
                       EmailConfirmationService emailConfirmationService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.jwtProperties = jwtProperties;
        this.emailConfirmationService = emailConfirmationService;
    }

    public AuthResponseDto login(LoginInput input) {
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(input.getEmail(), input.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new ProcessServiceException("Invalid email or password.");
        }

        User user = userRepository.findByEmail(input.getEmail())
            .orElseThrow(() -> new ProcessServiceException("User not found."));

        if (!Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new ProcessServiceException("Please confirm your email before logging in.");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getUserId(), user.getFirstName());
        Instant expiresAt = Instant.now().plusMillis(jwtProperties.getExpiration());

        return new AuthResponseDto(token, expiresAt, UserResponseDto.fromEntity(user));
    }

    public AuthResponseDto register(UserInput input) {
        UserResponseDto created = userService.postUser(input);

        User user = userRepository.findByEmail(input.getEmail())
            .orElseThrow(() -> new ProcessServiceException("Registration failed."));

        String token = jwtUtil.generateToken(user.getEmail(), user.getUserId(), user.getFirstName());
        Instant expiresAt = Instant.now().plusMillis(jwtProperties.getExpiration());

        emailConfirmationService.sendConfirmationEmail(user);

        return new AuthResponseDto(token, expiresAt, created);
    }
}
