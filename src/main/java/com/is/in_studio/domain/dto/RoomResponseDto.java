package com.is.in_studio.domain.dto;

import com.is.in_studio.entity.Room;

public record RoomResponseDto(
    Integer roomId,
    String name,
    Integer capacity,
    String location,
    String equipment,
    Boolean active,
    Integer layoutRows,
    Integer layoutCols,
    String layoutData
) {

    public static RoomResponseDto fromEntity(Room room) {
        return new RoomResponseDto(
            room.getRoomId(),
            room.getName(),
            room.getCapacity(),
            room.getLocation(),
            room.getEquipment(),
            room.getActive(),
            room.getLayoutRows(),
            room.getLayoutCols(),
            room.getLayoutData()
        );
    }
}
