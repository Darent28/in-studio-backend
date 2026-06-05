package com.is.in_studio.domain.input;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class RoomInput {

    @NotNull(message = "Name is required")
    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;

    private String location;
    private String equipment;
    private Boolean active = true;
    private Integer layoutRows = 0;
    private Integer layoutCols = 0;
    private String layoutData;

    public RoomInput() {
    }

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
