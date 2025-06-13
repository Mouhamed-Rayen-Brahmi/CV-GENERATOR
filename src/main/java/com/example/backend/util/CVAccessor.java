package com.example.backend.util;

import com.example.backend.model.CV;
import com.example.backend.model.Education;
import com.example.backend.model.Experience;
import com.example.backend.model.Skill;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CVAccessor {
    
    public List<Experience> getExperiences(CV cv) {
        return cv.getExperiences();
    }
    
    public List<Education> getEducations(CV cv) {
        return cv.getEducations();
    }
    
    public List<Skill> getSkills(CV cv) {
        return cv.getSkills();
    }
}