package com.is.in_studio.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.is.in_studio.domain.dto.RoomResponseDto;
import com.is.in_studio.domain.input.RoomInput;
import com.is.in_studio.entity.Room;
import com.is.in_studio.exception.CustomExceptions.NotFoundException;
import com.is.in_studio.exception.CustomExceptions.ServerErrorException;
import com.is.in_studio.repository.RoomRepository;

import jakarta.transaction.Transactional;

@Service
public class RoomService {

    private static final Logger log = LoggerFactory.getLogger(RoomService.class);

    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    public List<RoomResponseDto> getAll() {
        try {
            return roomRepository.findAll().stream()
                .map(RoomResponseDto::fromEntity)
                .toList();
        } catch (Exception e) {
            log.error("Failed to retrieve rooms", e);
            throw new ServerErrorException("Failed to retrieve rooms.");
        }
    }

    public RoomResponseDto getById(Integer id) {
        Room room = roomRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Room not found with id: " + id));
        return RoomResponseDto.fromEntity(room);
    }

    @Transactional
    public RoomResponseDto create(RoomInput input) {
        try {
            Room room = new Room();
            applyInput(room, input);
            roomRepository.save(room);
            return RoomResponseDto.fromEntity(room);
        } catch (Exception e) {
            log.error("Failed to create room", e);
            throw new ServerErrorException("Failed to create room.");
        }
    }

    @Transactional
    public RoomResponseDto update(Integer id, RoomInput input) {
        Room room = roomRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Room not found with id: " + id));
        try {
            applyInput(room, input);
            roomRepository.save(room);
            return RoomResponseDto.fromEntity(room);
        } catch (Exception e) {
            log.error("Failed to update room id={}", id, e);
            throw new ServerErrorException("Failed to update room.");
        }
    }

    @Transactional
    public void delete(Integer id) {
        roomRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Room not found with id: " + id));
        roomRepository.deleteById(id);
    }

    private void applyInput(Room room, RoomInput input) {
        room.setName(input.getName());
        room.setCapacity(input.getCapacity());
        room.setLocation(input.getLocation());
        room.setEquipment(input.getEquipment());
        room.setActive(input.getActive() != null ? input.getActive() : true);
    }
}
