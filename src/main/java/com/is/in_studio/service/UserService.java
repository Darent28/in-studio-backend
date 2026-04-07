package com.is.in_studio.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.is.in_studio.domain.dto.UserResponseDto;
import com.is.in_studio.domain.input.UserInput;
import com.is.in_studio.entity.User;
import com.is.in_studio.entity.User.GenderType;
import com.is.in_studio.exception.CustomExceptions.NotFoundException;
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
            GenderType gender = userInput.getGender() != null
                ? GenderType.valueOf(userInput.getGender())
                : null;

            User user = new User(
                userInput.getEmail(),
                passwordEncoder.encode(userInput.getPassword()),
                userInput.getFirstName(),
                userInput.getLastName(),
                userInput.getPhone(),
                userInput.getBirthdate(),
                gender
            );
            userRepository.save(user);
            return UserResponseDto.fromEntity(user);
        } catch (IllegalArgumentException e) {
            throw new com.is.in_studio.exception.CustomExceptions.ProcessServiceException(
                "Invalid gender value. Allowed: M, F, OTHER");
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
}
