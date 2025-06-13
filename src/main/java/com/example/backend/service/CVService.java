package com.example.backend.service;

import com.example.backend.dto.CVDto;
import com.example.backend.model.CV;
import com.example.backend.model.User;

import java.util.List;

public interface CVService {
    CV createCV(CVDto cvDto, User user);
    CV updateCV(Long id, CVDto cvDto, User user);
    CV findById(Long id);
    List<CV> findAllByUser(User user);
    CV findMostRecentByUser(User user);
    void deleteCV(Long id);
    CVDto convertToDto(CV cv);
    
    // Additional methods for API endpoints
    List<CVDto> getCVsByUserId(Long userId);
    CVDto getCVById(Long id);
    CVDto createCV(CVDto cvDto, Long userId);
    CVDto updateCV(Long id, CVDto cvDto);
}