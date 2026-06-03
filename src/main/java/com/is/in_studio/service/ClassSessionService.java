package com.is.in_studio.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.is.in_studio.domain.dto.ClassSessionResponseDto;
import com.is.in_studio.domain.input.ClassSessionInput;
import com.is.in_studio.entity.ClassSession;
import com.is.in_studio.entity.Discipline;
import com.is.in_studio.entity.Instructor;
import com.is.in_studio.entity.Room;
import com.is.in_studio.exception.CustomExceptions.NotFoundException;
import com.is.in_studio.exception.CustomExceptions.ServerErrorException;
import com.is.in_studio.repository.ClassSessionRepository;
import com.is.in_studio.repository.DisciplineRepository;
import com.is.in_studio.repository.InstructorRepository;
import com.is.in_studio.repository.RoomRepository;

import jakarta.transaction.Transactional;

@Service
public class ClassSessionService {

    private static final Logger log = LoggerFactory.getLogger(ClassSessionService.class);

    private final ClassSessionRepository sessionRepository;
    private final DisciplineRepository disciplineRepository;
    private final InstructorRepository instructorRepository;
    private final RoomRepository roomRepository;

    public ClassSessionService(ClassSessionRepository sessionRepository,
                               DisciplineRepository disciplineRepository,
                               InstructorRepository instructorRepository,
                               RoomRepository roomRepository) {
        this.sessionRepository = sessionRepository;
        this.disciplineRepository = disciplineRepository;
        this.instructorRepository = instructorRepository;
        this.roomRepository = roomRepository;
    }

    public List<ClassSessionResponseDto> getAll() {
        try {
            return sessionRepository.findAllWithDetails().stream()
                .map(ClassSessionResponseDto::fromEntity)
                .toList();
        } catch (Exception e) {
            log.error("Failed to retrieve sessions", e);
            throw new ServerErrorException("Failed to retrieve sessions.");
        }
    }

    public ClassSessionResponseDto getById(Long id) {
        ClassSession session = sessionRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Session not found with id: " + id));
        return ClassSessionResponseDto.fromEntity(session);
    }

    @Transactional
    public ClassSessionResponseDto create(ClassSessionInput input) {
        try {
            ClassSession session = new ClassSession();
            applyInput(session, input);
            sessionRepository.save(session);
            return ClassSessionResponseDto.fromEntity(session);
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to create session", e);
            throw new ServerErrorException("Failed to create session.");
        }
    }

    @Transactional
    public ClassSessionResponseDto update(Long id, ClassSessionInput input) {
        ClassSession session = sessionRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Session not found with id: " + id));
        try {
            applyInput(session, input);
            sessionRepository.save(session);
            return ClassSessionResponseDto.fromEntity(session);
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to update session id={}", id, e);
            throw new ServerErrorException("Failed to update session.");
        }
    }

    @Transactional
    public void delete(Long id) {
        sessionRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Session not found with id: " + id));
        sessionRepository.deleteById(id);
    }

    private void applyInput(ClassSession session, ClassSessionInput input) {
        Discipline discipline = disciplineRepository.findById(input.getDisciplineId())
            .orElseThrow(() -> new NotFoundException("Discipline not found with id: " + input.getDisciplineId()));
        Instructor instructor = instructorRepository.findById(input.getInstructorId())
            .orElseThrow(() -> new NotFoundException("Instructor not found with id: " + input.getInstructorId()));
        Room room = roomRepository.findById(input.getRoomId())
            .orElseThrow(() -> new NotFoundException("Room not found with id: " + input.getRoomId()));

        session.setDiscipline(discipline);
        session.setInstructor(instructor);
        session.setRoom(room);
        session.setStartDatetime(input.getStartDatetime());
        session.setEndDatetime(input.getEndDatetime());
        session.setCapacity(input.getCapacity());
        session.setNotes(input.getNotes());
        if (input.getStatus() != null) session.setStatus(input.getStatus());
    }
}
