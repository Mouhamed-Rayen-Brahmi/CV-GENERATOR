package com.example.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CVDto {
    
    private Long id;
    
    @NotBlank(message = "CV title is required")
    private String title;
    
    @NotBlank(message = "First name is required")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    private String lastName;
    
    @Email(message = "Please provide a valid email address")
    @NotBlank(message = "Email is required")
    private String email;
    
    private String phoneNumber;
    private String address;
    private String summary;
    private String profilePicture;
    private MultipartFile profilePictureFile;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private List<ExperienceDto> experiences = new ArrayList<>();
    private List<EducationDto> educations = new ArrayList<>();
    private List<SkillDto> skills = new ArrayList<>();

    // Constructors
    public CVDto() {
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public MultipartFile getProfilePictureFile() {
        return profilePictureFile;
    }

    public void setProfilePictureFile(MultipartFile profilePictureFile) {
        this.profilePictureFile = profilePictureFile;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<ExperienceDto> getExperiences() {
        return experiences;
    }

    public void setExperiences(List<ExperienceDto> experiences) {
        this.experiences = experiences != null ? experiences : new ArrayList<>();
    }

    public List<EducationDto> getEducations() {
        return educations;
    }

    public void setEducations(List<EducationDto> educations) {
        this.educations = educations != null ? educations : new ArrayList<>();
    }

    public List<SkillDto> getSkills() {
        return skills;
    }

    public void setSkills(List<SkillDto> skills) {
        this.skills = skills != null ? skills : new ArrayList<>();
    }
}