package com.example.backend.repository;

import com.example.backend.model.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    
    Optional<VerificationToken> findByToken(String token);
    
    Optional<VerificationToken> findByEmail(String email);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM VerificationToken v WHERE v.expiryDate < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);
    
    @Modifying
    @Transactional
    void deleteByEmail(String email);
}