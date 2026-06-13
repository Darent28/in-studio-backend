package com.is.in_studio.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.is.in_studio.auth.JwtUtil;
import com.is.in_studio.domain.dto.ReservationResponseDto;
import com.is.in_studio.exception.CustomExceptions.BadRequestException;
import com.is.in_studio.exception.CustomExceptions.NotFoundException;
import com.is.in_studio.service.ReservationService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;
    private final JwtUtil jwtUtil;

    public ReservationController(ReservationService reservationService, JwtUtil jwtUtil) {
        this.reservationService = reservationService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponseDto book(@RequestBody Map<String, Object> body,
                                        HttpServletRequest request) {
        Long userId = extractUserId(request);
        Long sessionId = ((Number) body.get("sessionId")).longValue();
        String dateStr = (String) body.get("sessionDate");
        LocalDate sessionDate = LocalDate.parse(dateStr);
        return reservationService.book(userId, sessionId, sessionDate);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@PathVariable Long id, HttpServletRequest request) {
        Long userId = extractUserId(request);
        reservationService.cancel(id, userId);
    }

    @GetMapping("/my")
    public List<ReservationResponseDto> getMyReservations(HttpServletRequest request) {
        Long userId = extractUserId(request);
        return reservationService.getMyReservations(userId);
    }

    @ExceptionHandler({BadRequestException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleBadRequest(BadRequestException e) {
        return Map.of("message", e.getMessage());
    }

    @ExceptionHandler({NotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleNotFound(NotFoundException e) {
        return Map.of("message", e.getMessage());
    }

    private Long extractUserId(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        return jwtUtil.extractUserId(header.substring(7));
    }
}
