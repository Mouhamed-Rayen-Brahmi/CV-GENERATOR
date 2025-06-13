package com.example.backend.controller;

import com.example.backend.dto.CVDto;
import com.example.backend.service.CVService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cvs")
public class CVController {

    private final CVService cvService;

    public CVController(CVService cvService) {
        this.cvService = cvService;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CVDto>> getCVsByUserId(@PathVariable Long userId) {
        List<CVDto> cvs = cvService.getCVsByUserId(userId);
        return ResponseEntity.ok(cvs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CVDto> getCVById(@PathVariable Long id) {
        CVDto cv = cvService.getCVById(id);
        return ResponseEntity.ok(cv);
    }

    @PostMapping("/user/{userId}")
    public ResponseEntity<CVDto> createCV(
            @PathVariable Long userId,
            @Valid @RequestBody CVDto cvDto
    ) {
        CVDto createdCV = cvService.createCV(cvDto, userId);
        return new ResponseEntity<>(createdCV, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CVDto> updateCV(
            @PathVariable Long id,
            @Valid @RequestBody CVDto cvDto
    ) {
        CVDto updatedCV = cvService.updateCV(id, cvDto);
        return ResponseEntity.ok(updatedCV);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCV(@PathVariable Long id) {
        cvService.deleteCV(id);
        return ResponseEntity.noContent().build();
    }
}