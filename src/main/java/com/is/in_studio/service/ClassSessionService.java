package com.is.in_studio.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.is.in_studio.domain.dto.AdminSessionScheduleDto;
import com.is.in_studio.domain.dto.ClassSessionResponseDto;
import com.is.in_studio.domain.input.ClassSessionInput;
import com.is.in_studio.entity.ClassSession;
import com.is.in_studio.entity.Instructor;
import com.is.in_studio.entity.Reservation;
import com.is.in_studio.entity.Room;
import com.is.in_studio.exception.CustomExceptions.NotFoundException;
import com.is.in_studio.exception.CustomExceptions.ServerErrorException;
import com.is.in_studio.repository.ClassSessionRepository;
import com.is.in_studio.repository.InstructorRepository;
import com.is.in_studio.repository.ReservationRepository;
import com.is.in_studio.repository.RoomRepository;

import jakarta.transaction.Transactional;

@Service
public class ClassSessionService {

    private static final Logger log = LoggerFactory.getLogger(ClassSessionService.class);

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private final ClassSessionRepository sessionRepository;
    private final InstructorRepository instructorRepository;
    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;

    public ClassSessionService(ClassSessionRepository sessionRepository,
                               InstructorRepository instructorRepository,
                               RoomRepository roomRepository,
                               ReservationRepository reservationRepository) {
        this.sessionRepository = sessionRepository;
        this.instructorRepository = instructorRepository;
        this.roomRepository = roomRepository;
        this.reservationRepository = reservationRepository;
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

    public List<AdminSessionScheduleDto> getSchedule(LocalDate date, Integer instructorId) {
        int bit = 1 << (date.getDayOfWeek().getValue() - 1);
        return sessionRepository.findAllWithDetails().stream()
            .filter(s -> s.getStatus() == ClassSession.SessionStatus.SCHEDULED)
            .filter(s -> (s.getDaysOfWeek() & bit) != 0)
            .filter(s -> instructorId == null || s.getInstructor().getInstructorId().equals(instructorId))
            .sorted(Comparator.comparing(ClassSession::getStartTime))
            .map(s -> {
                List<Reservation> reservations = reservationRepository.findActiveBySessionAndDate(s.getSessionId(), date);
                long reservedCount = reservations.stream().filter(r -> "RESERVED".equals(r.getStatus())).count();
                long onHoldCount  = reservations.stream().filter(r -> "ON_HOLD".equals(r.getStatus())).count();
                List<AdminSessionScheduleDto.AttendeeDto> attendees = reservations.stream()
                    .map(r -> new AdminSessionScheduleDto.AttendeeDto(
                        r.getReservationId(),
                        r.getUser().getUserId(),
                        r.getUser().getFirstName(),
                        r.getUser().getLastName(),
                        r.getUser().getEmail(),
                        r.getStatus()
                    ))
                    .toList();
                return new AdminSessionScheduleDto(
                    s.getSessionId(),
                    s.getTitle(),
                    s.getInstructor().getInstructorId(),
                    s.getInstructor().getUser().getFirstName(),
                    s.getInstructor().getUser().getLastName(),
                    s.getRoom().getName(),
                    s.getStartTime().format(TIME_FMT),
                    s.getEndTime().format(TIME_FMT),
                    s.getRoom().getCapacity(),
                    reservedCount,
                    onHoldCount,
                    attendees
                );
            })
            .toList();
    }

    private void applyInput(ClassSession session, ClassSessionInput input) {
        Instructor instructor = instructorRepository.findById(input.getInstructorId())
            .orElseThrow(() -> new NotFoundException("Instructor not found with id: " + input.getInstructorId()));
        Room room = roomRepository.findById(input.getRoomId())
            .orElseThrow(() -> new NotFoundException("Room not found with id: " + input.getRoomId()));

        session.setInstructor(instructor);
        session.setRoom(room);
        session.setStartTime(LocalTime.parse(input.getStartTime(), DateTimeFormatter.ofPattern("HH:mm")));
        session.setEndTime(LocalTime.parse(input.getEndTime(), DateTimeFormatter.ofPattern("HH:mm")));
        session.setDaysOfWeek(daysToBitmask(input.getDays()));
        session.setTitle(input.getTitle());
        session.setNotes(input.getNotes());
        if (input.getStatus() != null) session.setStatus(input.getStatus());
    }

    private static int daysToBitmask(List<String> days) {
        if (days == null || days.isEmpty()) return 0;
        return days.stream()
            .map(String::toUpperCase)
            .map(DayOfWeek::valueOf)
            .mapToInt(d -> 1 << (d.getValue() - 1))
            .sum();
    }
}
