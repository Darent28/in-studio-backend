package com.is.in_studio.entity;

import java.io.Serializable;

import jakarta.persistence.*;

@Entity
@Table(name = "discipline")
public class Discipline implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "discipline_id")
    private Integer disciplineId;

    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "color_hex", length = 7)
    private String colorHex;

    @Column(name = "icon", length = 50)
    private String icon;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    public Discipline() {
    }

    public Integer getDisciplineId() { return disciplineId; }
    public void setDisciplineId(Integer disciplineId) { this.disciplineId = disciplineId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getColorHex() { return colorHex; }
    public void setColorHex(String colorHex) { this.colorHex = colorHex; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
