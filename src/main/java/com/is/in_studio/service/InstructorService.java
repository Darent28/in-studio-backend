package com.is.in_studio.service;

import org.springframework.stereotype.Service;

import com.is.in_studio.domain.dto.InstructorDto;
import com.is.in_studio.domain.input.InstructorInput;
import com.is.in_studio.domain.input.InstructorUpdateInput;
import com.is.in_studio.entity.Instructor;
import com.is.in_studio.entity.User;
import com.is.in_studio.exception.CustomExceptions.NotFoundException;
import com.is.in_studio.repository.InstructorRepository;
import com.is.in_studio.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class InstructorService {

    private final InstructorRepository instructorRepository;
    private final UserRepository userRepository;

    public InstructorService(InstructorRepository instructorRepository, UserRepository userRepository) {
        this.instructorRepository = instructorRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public InstructorDto create(InstructorInput input) {
        User user = userRepository.findById(input.getUserId())
            .orElseThrow(() -> new NotFoundException("User not found with id: " + input.getUserId()));

        Instructor instructor = new Instructor();
        instructor.setUser(user);
        instructor.setBio(input.getBio());
        instructor.setSpecialty(input.getSpecialty());
        instructor.setActive(true);
        instructorRepository.save(instructor);
        return toDto(instructor);
    }

    @Transactional
    public InstructorDto update(Integer id, InstructorUpdateInput input) {
        Instructor instructor = instructorRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Instructor not found with id: " + id));
        instructor.setBio(input.getBio());
        instructor.setSpecialty(input.getSpecialty());
        if (input.getActive() != null) instructor.setActive(input.getActive());
        instructorRepository.save(instructor);
        return toDto(instructor);
    }

    @Transactional
    public void delete(Integer id) {
        instructorRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Instructor not found with id: " + id));
        instructorRepository.deleteById(id);
    }

    public static InstructorDto toDto(Instructor i) {
        return new InstructorDto(
            i.getInstructorId(),
            i.getUser().getUserId(),
            i.getUser().getFirstName(),
            i.getUser().getLastName(),
            i.getUser().getEmail(),
            i.getSpecialty(),
            i.getBio(),
            i.getActive()
        );
    }
}
