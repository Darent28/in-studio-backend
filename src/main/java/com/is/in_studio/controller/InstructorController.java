package com.is.in_studio.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.is.in_studio.domain.dto.InstructorDto;
import com.is.in_studio.repository.InstructorRepository;

@RestController
@RequestMapping("/api/admin/instructors")
public class InstructorController {

    private final InstructorRepository instructorRepository;

    public InstructorController(InstructorRepository instructorRepository) {
        this.instructorRepository = instructorRepository;
    }

    @GetMapping
    public List<InstructorDto> getAll() {
        return instructorRepository.findAll().stream()
            .map(i -> new InstructorDto(i.getInstructorId(), i.getUser().getFirstName(), i.getUser().getLastName()))
            .toList();
    }
}
