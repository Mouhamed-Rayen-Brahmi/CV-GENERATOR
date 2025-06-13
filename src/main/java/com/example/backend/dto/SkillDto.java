package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class SkillDto {
    
    private Long id;
    
    @NotBlank(message = "Skill name is required")
    private String name;
    
    private String proficiencyLevel;

    // Constructors
    public SkillDto() {
    }

    public SkillDto(String name, String proficiencyLevel) {
        this.name = name;
        this.proficiencyLevel = proficiencyLevel;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProficiencyLevel() {
        return proficiencyLevel;
    }

    public void setProficiencyLevel(String proficiencyLevel) {
        this.proficiencyLevel = proficiencyLevel;
    }
}