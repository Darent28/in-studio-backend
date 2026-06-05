package com.is.in_studio.domain.input;

import jakarta.validation.constraints.NotNull;

public class InstructorInput {

    @NotNull(message = "userId is required")
    private Long userId;

    private String bio;
    private String specialty;

    public InstructorInput() {}

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }
}
