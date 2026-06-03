package com.is.in_studio.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.is.in_studio.domain.dto.RoomResponseDto;
import com.is.in_studio.domain.input.RoomInput;
import com.is.in_studio.service.RoomService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    public List<RoomResponseDto> getAll() {
        return roomService.getAll();
    }

    @GetMapping("/{id}")
    public RoomResponseDto getById(@PathVariable Integer id) {
        return roomService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RoomResponseDto create(@Valid @RequestBody RoomInput input) {
        return roomService.create(input);
    }

    @PutMapping("/{id}")
    public RoomResponseDto update(@PathVariable Integer id, @Valid @RequestBody RoomInput input) {
        return roomService.update(id, input);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        roomService.delete(id);
    }
}
