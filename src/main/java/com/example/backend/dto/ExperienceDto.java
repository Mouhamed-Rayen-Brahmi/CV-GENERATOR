package com.example.backend.dto;

import com.example.backend.validation.PastOrPresentDate;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public class ExperienceDto {
    
    private Long id;
    
    @NotBlank(message = "Position is required")
    private String position;
    
    @NotBlank(message = "Company is required")
    private String company;
    
    @PastOrPresentDate(message = "Start date must be in the past or present")
    private LocalDate startDate;
    
    private LocalDate endDate;
    
    private String location;
    private String description;
    private boolean currentJob;

    // Constructors
    public ExperienceDto() {
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isCurrentJob() {
        return currentJob;
    }

    public void setCurrentJob(boolean currentJob) {
        this.currentJob = currentJob;
    }
}