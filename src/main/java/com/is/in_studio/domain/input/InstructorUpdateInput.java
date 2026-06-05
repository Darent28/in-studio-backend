package com.is.in_studio.domain.input;

public class InstructorUpdateInput {

    private String bio;
    private String specialty;
    private Boolean active;

    public InstructorUpdateInput() {}

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
