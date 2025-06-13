package com.example.backend.service.impl;

import com.example.backend.dto.CVDto;
import com.example.backend.dto.EducationDto;
import com.example.backend.dto.ExperienceDto;
import com.example.backend.dto.SkillDto;
import com.example.backend.model.*;
import com.example.backend.repository.CVRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.CVService;
import com.example.backend.service.FileUploadService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CVServiceImpl implements CVService {

    private final CVRepository cvRepository;
    private final UserRepository userRepository;
    private final FileUploadService fileUploadService;

    public CVServiceImpl(CVRepository cvRepository, UserRepository userRepository,
                        FileUploadService fileUploadService) {
        this.cvRepository = cvRepository;
        this.userRepository = userRepository;
        this.fileUploadService = fileUploadService;
    }

    @Override
    public CV createCV(CVDto cvDto, User user) {
        CV cv = new CV();
        mapDtoToEntity(cvDto, cv);
        cv.setUser(user);
        return cvRepository.save(cv);
    }

    @Override
    public CV updateCV(Long id, CVDto cvDto, User user) {
        CV cv = findById(id);

        // Check if user owns this CV
        if (!cv.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to CV");
        }

        // Delete old profile picture if a new one is uploaded
        if (cvDto.getProfilePictureFile() != null && !cvDto.getProfilePictureFile().isEmpty()) {
            if (cv.getProfilePicture() != null) {
                fileUploadService.deleteProfilePicture(cv.getProfilePicture());
            }
        }

        mapDtoToEntity(cvDto, cv);
        return cvRepository.save(cv);
    }

    @Override
    @Transactional(readOnly = true)
    public CV findById(Long id) {
        return cvRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CV not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CV> findAllByUser(User user) {
        return cvRepository.findByUserOrderByUpdatedAtDesc(user);
    }

    @Override
    @Transactional(readOnly = true)
    public CV findMostRecentByUser(User user) {
        return cvRepository.findFirstByUserOrderByUpdatedAtDesc(user);
    }

    @Override
    public void deleteCV(Long id) {
        CV cv = findById(id);

        // Delete associated profile picture
        if (cv.getProfilePicture() != null) {
            fileUploadService.deleteProfilePicture(cv.getProfilePicture());
        }

        cvRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public CVDto convertToDto(CV cv) {
        CVDto dto = new CVDto();
        dto.setId(cv.getId());
        dto.setTitle(cv.getTitle());
        dto.setFirstName(cv.getFirstName());
        dto.setLastName(cv.getLastName());
        dto.setEmail(cv.getEmail());
        dto.setPhoneNumber(cv.getPhoneNumber());
        dto.setAddress(cv.getAddress());
        dto.setSummary(cv.getSummary());
        dto.setProfilePicture(cv.getProfilePicture());
        dto.setCreatedAt(cv.getCreatedAt());
        dto.setUpdatedAt(cv.getUpdatedAt());

        // Convert experiences
        if (cv.getExperiences() != null) {
            List<ExperienceDto> experienceDtos = cv.getExperiences().stream()
                    .map(this::convertExperienceToDto)
                    .collect(Collectors.toList());
            dto.setExperiences(experienceDtos);
        }

        // Convert educations
        if (cv.getEducations() != null) {
            List<EducationDto> educationDtos = cv.getEducations().stream()
                    .map(this::convertEducationToDto)
                    .collect(Collectors.toList());
            dto.setEducations(educationDtos);
        }

        // Convert skills
        if (cv.getSkills() != null) {
            List<SkillDto> skillDtos = cv.getSkills().stream()
                    .map(this::convertSkillToDto)
                    .collect(Collectors.toList());
            dto.setSkills(skillDtos);
        }

        return dto;
    }

    // API methods
    @Override
    @Transactional(readOnly = true)
    public List<CVDto> getCVsByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        List<CV> cvs = findAllByUser(user);
        return cvs.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CVDto getCVById(Long id) {
        CV cv = findById(id);
        return convertToDto(cv);
    }

    @Override
    public CVDto createCV(CVDto cvDto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        CV cv = createCV(cvDto, user);
        return convertToDto(cv);
    }

    @Override
    public CVDto updateCV(Long id, CVDto cvDto) {
        CV cv = findById(id);
        mapDtoToEntity(cvDto, cv);
        CV updatedCV = cvRepository.save(cv);
        return convertToDto(updatedCV);
    }

    // Helper methods
    private void mapDtoToEntity(CVDto dto, CV cv) {
        cv.setTitle(dto.getTitle());
        cv.setFirstName(dto.getFirstName());
        cv.setLastName(dto.getLastName());
        cv.setEmail(dto.getEmail());
        cv.setPhoneNumber(dto.getPhoneNumber());
        cv.setAddress(dto.getAddress());
        cv.setSummary(dto.getSummary());
        
        // Only set profile picture if it's provided
        if (dto.getProfilePicture() != null && !dto.getProfilePicture().isEmpty()) {
            cv.setProfilePicture(dto.getProfilePicture());
        }

        // Clear existing collections to avoid duplicates
        cv.getExperiences().clear();
        cv.getEducations().clear();
        cv.getSkills().clear();

        // Map experiences
        if (dto.getExperiences() != null) {
            for (ExperienceDto expDto : dto.getExperiences()) {
                if (expDto.getPosition() != null && !expDto.getPosition().trim().isEmpty()) {
                    Experience experience = convertDtoToExperience(expDto);
                    experience.setCv(cv);
                    cv.getExperiences().add(experience);
                }
            }
        }

        // Map educations
        if (dto.getEducations() != null) {
            for (EducationDto eduDto : dto.getEducations()) {
                if (eduDto.getDegree() != null && !eduDto.getDegree().trim().isEmpty()) {
                    Education education = convertDtoToEducation(eduDto);
                    education.setCv(cv);
                    cv.getEducations().add(education);
                }
            }
        }

        // Map skills
        if (dto.getSkills() != null) {
            for (SkillDto skillDto : dto.getSkills()) {
                if (skillDto.getName() != null && !skillDto.getName().trim().isEmpty()) {
                    Skill skill = convertDtoToSkill(skillDto);
                    skill.setCv(cv);
                    cv.getSkills().add(skill);
                }
            }
        }
    }

    private ExperienceDto convertExperienceToDto(Experience experience) {
        ExperienceDto dto = new ExperienceDto();
        dto.setId(experience.getId());
        dto.setPosition(experience.getPosition());
        dto.setCompany(experience.getCompany());
        dto.setStartDate(experience.getStartDate());
        dto.setEndDate(experience.getEndDate());
        dto.setLocation(experience.getLocation());
        dto.setDescription(experience.getDescription());
        dto.setCurrentJob(experience.isCurrentJob());
        return dto;
    }

    private EducationDto convertEducationToDto(Education education) {
        EducationDto dto = new EducationDto();
        dto.setId(education.getId());
        dto.setDegree(education.getDegree());
        dto.setInstitution(education.getInstitution());
        dto.setFieldOfStudy(education.getFieldOfStudy());
        dto.setStartDate(education.getStartDate());
        dto.setEndDate(education.getEndDate());
        dto.setDescription(education.getDescription());
        return dto;
    }

    private SkillDto convertSkillToDto(Skill skill) {
        SkillDto dto = new SkillDto();
        dto.setId(skill.getId());
        dto.setName(skill.getName());
        dto.setProficiencyLevel(skill.getProficiencyLevel());
        return dto;
    }

    private Experience convertDtoToExperience(ExperienceDto dto) {
        Experience experience = new Experience();
        experience.setId(dto.getId());
        experience.setPosition(dto.getPosition());
        experience.setCompany(dto.getCompany());
        experience.setStartDate(dto.getStartDate());
        experience.setEndDate(dto.getEndDate());
        experience.setLocation(dto.getLocation());
        experience.setDescription(dto.getDescription());
        experience.setCurrentJob(dto.isCurrentJob());
        return experience;
    }

    private Education convertDtoToEducation(EducationDto dto) {
        Education education = new Education();
        education.setId(dto.getId());
        education.setDegree(dto.getDegree());
        education.setInstitution(dto.getInstitution());
        education.setFieldOfStudy(dto.getFieldOfStudy());
        education.setStartDate(dto.getStartDate());
        education.setEndDate(dto.getEndDate());
        education.setDescription(dto.getDescription());
        return education;
    }

    private Skill convertDtoToSkill(SkillDto dto) {
        Skill skill = new Skill();
        skill.setId(dto.getId());
        skill.setName(dto.getName());
        skill.setProficiencyLevel(dto.getProficiencyLevel());
        return skill;
    }
}