package com.is.in_studio.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.is.in_studio.domain.dto.InstructorDto;
import com.is.in_studio.domain.dto.UserSearchDto;
import com.is.in_studio.domain.input.InstructorInput;
import com.is.in_studio.domain.input.InstructorUpdateInput;
import com.is.in_studio.repository.InstructorRepository;
import com.is.in_studio.repository.UserRepository;
import com.is.in_studio.service.InstructorService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/instructors")
public class InstructorController {

    private final InstructorRepository instructorRepository;
    private final UserRepository userRepository;
    private final InstructorService instructorService;

    public InstructorController(InstructorRepository instructorRepository,
                                UserRepository userRepository,
                                InstructorService instructorService) {
        this.instructorRepository = instructorRepository;
        this.userRepository = userRepository;
        this.instructorService = instructorService;
    }

    @GetMapping
    public List<InstructorDto> getAll() {
        return instructorRepository.findAll().stream()
            .map(InstructorService::toDto)
            .toList();
    }

    @GetMapping("/user-search")
    public List<UserSearchDto> searchUsers(@RequestParam String q) {
        if (q == null || q.isBlank()) return List.of();
        Set<Long> existing = new HashSet<>(instructorRepository.findAllUserIds());
        return userRepository.searchByNameOrEmail(q).stream()
            .filter(u -> !existing.contains(u.getUserId()))
            .map(u -> new UserSearchDto(u.getUserId(), u.getFirstName(), u.getLastName(), u.getEmail()))
            .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InstructorDto create(@RequestBody @Valid InstructorInput input) {
        return instructorService.create(input);
    }

    @PutMapping("/{id}")
    public InstructorDto update(@PathVariable Integer id, @RequestBody InstructorUpdateInput input) {
        return instructorService.update(id, input);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        instructorService.delete(id);
    }
}
