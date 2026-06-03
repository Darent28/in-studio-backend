package com.is.in_studio.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.is.in_studio.domain.dto.AdminUserResponseDto;
import com.is.in_studio.domain.input.AdminUserInput;
import com.is.in_studio.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<AdminUserResponseDto> getAll() {
        return userService.adminGetAllUsers();
    }

    @GetMapping("/{id}")
    public AdminUserResponseDto getById(@PathVariable Long id) {
        return userService.adminGetUserById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AdminUserResponseDto create(@Valid @RequestBody AdminUserInput input) {
        return userService.adminCreateUser(input);
    }

    @PutMapping("/{id}")
    public AdminUserResponseDto update(@PathVariable Long id, @Valid @RequestBody AdminUserInput input) {
        return userService.adminUpdateUser(id, input);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        userService.adminDeleteUser(id);
    }
}
