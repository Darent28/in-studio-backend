package com.is.in_studio.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.is.in_studio.domain.dto.AdminSessionScheduleDto;
import com.is.in_studio.domain.dto.ClassSessionResponseDto;
import com.is.in_studio.domain.input.ClassSessionInput;
import com.is.in_studio.service.ClassSessionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/sessions")
public class ClassSessionController {

    private final ClassSessionService sessionService;

    public ClassSessionController(ClassSessionService sessionService) {
        this.sessionService = sessionService;
    }

    @GetMapping
    public List<ClassSessionResponseDto> getAll() {
        return sessionService.getAll();
    }

    @GetMapping("/schedule")
    public List<AdminSessionScheduleDto> getSchedule(
            @RequestParam(required = false) String date,
            @RequestParam(required = false) Integer instructorId) {
        LocalDate target = (date != null && !date.isBlank()) ? LocalDate.parse(date) : LocalDate.now();
        return sessionService.getSchedule(target, instructorId);
    }

    @GetMapping("/{id}")
    public ClassSessionResponseDto getById(@PathVariable Long id) {
        return sessionService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClassSessionResponseDto create(@Valid @RequestBody ClassSessionInput input) {
        return sessionService.create(input);
    }

    @PutMapping("/{id}")
    public ClassSessionResponseDto update(@PathVariable Long id, @Valid @RequestBody ClassSessionInput input) {
        return sessionService.update(id, input);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        sessionService.delete(id);
    }
}
