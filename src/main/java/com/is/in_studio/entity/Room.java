package com.is.in_studio.entity;

import java.io.Serializable;

import jakarta.persistence.*;

@Entity
@Table(name = "room")
public class Room implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private Integer roomId;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @Column(name = "location", length = 150)
    private String location;

    @Column(name = "equipment")
    private String equipment;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "layout_rows", nullable = false)
    private Integer layoutRows = 0;

    @Column(name = "layout_cols", nullable = false)
    private Integer layoutCols = 0;

    @Column(name = "layout_data", columnDefinition = "TEXT")
    private String layoutData;

    public Room() {
    }

    public Integer getRoomId() { return roomId; }
    public void setRoomId(Integer roomId) { this.roomId = roomId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getEquipment() { return equipment; }
    public void setEquipment(String equipment) { this.equipment = equipment; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public Integer getLayoutRows() { return layoutRows; }
    public void setLayoutRows(Integer layoutRows) { this.layoutRows = layoutRows; }

    public Integer getLayoutCols() { return layoutCols; }
    public void setLayoutCols(Integer layoutCols) { this.layoutCols = layoutCols; }

    public String getLayoutData() { return layoutData; }
    public void setLayoutData(String layoutData) { this.layoutData = layoutData; }
}
