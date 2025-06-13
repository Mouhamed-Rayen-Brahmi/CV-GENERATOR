package com.example.backend.dto;

import com.example.backend.validation.PastOrPresentDate;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public class EducationDto {
    
    private Long id;
    
    @NotBlank(message = "Degree is required")
    private String degree;
    
    @NotBlank(message = "Institution is required")
    private String institution;
    
    private String fieldOfStudy;
    
    @PastOrPresentDate(message = "Start date must be in the past or present")
    private LocalDate startDate;
    
    private LocalDate endDate;
    
    private String description;

    // Constructors
    public EducationDto() {
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDegree() {
        return degree;
    }

    public void setDegree(String degree) {
        this.degree = degree;
    }

    public String getInstitution() {
        return institution;
    }

    public void setInstitution(String institution) {
        this.institution = institution;
    }

    public String getFieldOfStudy() {
        return fieldOfStudy;
    }

    public void setFieldOfStudy(String fieldOfStudy) {
        this.fieldOfStudy = fieldOfStudy;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}