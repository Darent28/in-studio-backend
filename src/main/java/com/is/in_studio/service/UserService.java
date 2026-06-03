package com.is.in_studio.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.is.in_studio.domain.dto.AdminUserResponseDto;
import com.is.in_studio.domain.dto.UserResponseDto;
import com.is.in_studio.domain.input.AdminUserInput;
import com.is.in_studio.domain.input.UserInput;
import com.is.in_studio.entity.User;
import com.is.in_studio.exception.CustomExceptions.NotFoundException;
import com.is.in_studio.exception.CustomExceptions.ProcessServiceException;
import com.is.in_studio.exception.CustomExceptions.ServerErrorException;

import com.is.in_studio.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponseDto postUser(UserInput userInput) {
        try {
            User user = new User(
                userInput.getEmail(),
                passwordEncoder.encode(userInput.getPassword()),
                userInput.getFirstName(),
                userInput.getLastName(),
                userInput.getPhone(),
                userInput.getBirthdate()
            );
            userRepository.save(user);
            return UserResponseDto.fromEntity(user);
        } catch (Exception e) {
            log.error("Failed to create user", e);
            throw new ServerErrorException("Failed to create user. Please try again later.");
        }
    }

    public List<UserResponseDto> getUsers() {
        try {
            return userRepository.findAll().stream()
                .map(UserResponseDto::fromEntity)
                .toList();
        } catch (Exception e) {
            log.error("Failed to retrieve users", e);
            throw new ServerErrorException("Failed to retrieve users. Please try again later.");
        }
    }

    public UserResponseDto getUserById(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("User not found with id: " + id));
        return UserResponseDto.fromEntity(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        userRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("User not found with id: " + id));
        userRepository.deactivateById(id);
    }

    // ── Admin CRUD ──────────────────────────────────────────────────────────────

    @Transactional
    public AdminUserResponseDto adminCreateUser(AdminUserInput input) {
        if (userRepository.findByEmail(input.getEmail()).isPresent()) {
            throw new ProcessServiceException("Email already in use: " + input.getEmail());
        }
        if (input.getPassword() == null || input.getPassword().isBlank()) {
            throw new ProcessServiceException("Password is required when creating a user.");
        }
        try {
            User user = new User(
                input.getEmail(),
                passwordEncoder.encode(input.getPassword()),
                input.getFirstName(),
                input.getLastName(),
                input.getPhone(),
                input.getBirthdate()
            );
            if (input.getRole() != null) user.setRole(input.getRole());
            if (input.getActive() != null) user.setActive(input.getActive());
            userRepository.save(user);
            return AdminUserResponseDto.fromEntity(user);
        } catch (ProcessServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to create user (admin)", e);
            throw new ServerErrorException("Failed to create user. Please try again later.");
        }
    }

    public List<AdminUserResponseDto> adminGetAllUsers() {
        try {
            return userRepository.findAll().stream()
                .map(AdminUserResponseDto::fromEntity)
                .toList();
        } catch (Exception e) {
            log.error("Failed to retrieve users", e);
            throw new ServerErrorException("Failed to retrieve users. Please try again later.");
        }
    }

    public AdminUserResponseDto adminGetUserById(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("User not found with id: " + id));
        return AdminUserResponseDto.fromEntity(user);
    }

    @Transactional
    public AdminUserResponseDto adminUpdateUser(Long id, AdminUserInput input) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("User not found with id: " + id));
        try {
            user.setFirstName(input.getFirstName());
            user.setLastName(input.getLastName());
            user.setEmail(input.getEmail());
            user.setPhone(input.getPhone());
            user.setBirthdate(input.getBirthdate());
            if (input.getRole() != null) user.setRole(input.getRole());
            if (input.getActive() != null) user.setActive(input.getActive());
            if (input.getPassword() != null && !input.getPassword().isBlank()) {
                user.setPasswordHash(passwordEncoder.encode(input.getPassword()));
            }
            userRepository.save(user);
            return AdminUserResponseDto.fromEntity(user);
        } catch (Exception e) {
            log.error("Failed to update user id={}", id, e);
            throw new ServerErrorException("Failed to update user. Please try again later.");
        }
    }

    @Transactional
    public void adminDeleteUser(Long id) {
        userRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("User not found with id: " + id));
        userRepository.deleteById(id);
    }
}
