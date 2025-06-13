package com.example.backend.service;

import com.example.backend.model.User;
import com.example.backend.model.VerificationToken;
import com.example.backend.repository.UserRepository;
import com.example.backend.repository.VerificationTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class VerificationService {

    private static final Logger logger = LoggerFactory.getLogger(VerificationService.class);
    private static final String CHARACTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int CODE_LENGTH = 6;

    private final VerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final SecureRandom random = new SecureRandom();

    @Value("${app.verification-token-expiry}")
    private long tokenExpiryMillis;

    public VerificationService(VerificationTokenRepository tokenRepository, 
                             UserRepository userRepository, 
                             EmailService emailService) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }
    public void sendVerificationEmail(User user) {
        Optional<VerificationToken> tokenOpt = tokenRepository.findByEmail(user.getEmail());
        if (tokenOpt.isPresent()) {
            VerificationToken token = tokenOpt.get();
            emailService.sendVerificationEmail(user.getEmail(), token.getToken(), user.getFirstName());
            logger.info("Verification email sent to: {}", user.getEmail());
        } else {
            logger.warn("Attempted to send verification email but no token found for: {}", user.getEmail());
        }
    }

    @Transactional
    public void createVerificationToken(User user) {
        // Delete any existing tokens for this email
        tokenRepository.deleteByEmail(user.getEmail());

        // Generate verification code
        String verificationCode = generateVerificationCode();
        
        // Create expiry date
        LocalDateTime expiryDate = LocalDateTime.now().plusSeconds(tokenExpiryMillis / 1000);
        
        // Create and save token
        VerificationToken token = new VerificationToken(verificationCode, user.getEmail(), expiryDate);
        tokenRepository.save(token);
        
        // Send verification email
        emailService.sendVerificationEmail(user.getEmail(), verificationCode, user.getFirstName());
        
        logger.info("Verification token created for email: {}", user.getEmail());
    }

    @Transactional
    public boolean verifyEmail(String token) {
        Optional<VerificationToken> verificationToken = tokenRepository.findByToken(token);
        
        if (verificationToken.isEmpty()) {
            logger.warn("Invalid verification token: {}", token);
            return false;
        }
        
        VerificationToken vToken = verificationToken.get();
        
        if (vToken.isExpired()) {
            logger.warn("Expired verification token for email: {}", vToken.getEmail());
            tokenRepository.delete(vToken);
            return false;
        }
        
        if (vToken.isVerified()) {
            logger.warn("Already verified token for email: {}", vToken.getEmail());
            return false;
        }
        
        // Find user and mark as verified
        Optional<User> userOpt = userRepository.findByEmail(vToken.getEmail());
        if (userOpt.isEmpty()) {
            logger.error("User not found for email: {}", vToken.getEmail());
            return false;
        }
        
        User user = userOpt.get();
        user.setEmailVerified(true);
        userRepository.save(user);
        
        // Mark token as verified
        vToken.setVerified(true);
        tokenRepository.save(vToken);
        
        logger.info("Email verified successfully for: {}", vToken.getEmail());
        return true;
    }

    public boolean resendVerificationCode(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            logger.warn("Attempt to resend verification for non-existent email: {}", email);
            return false;
        }
        
        User user = userOpt.get();
        if (user.isEmailVerified()) {
            logger.warn("Attempt to resend verification for already verified email: {}", email);
            return false;
        }
        
        createVerificationToken(user);
        return true;
    }

    private String generateVerificationCode() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return code.toString();
    }

    // Clean up expired tokens every hour
    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void cleanupExpiredTokens() {
        tokenRepository.deleteExpiredTokens(LocalDateTime.now());
        logger.info("Cleaned up expired verification tokens");
    }

    public boolean isEmailVerified(String email) {
        return userRepository.findByEmail(email)
                .map(User::isEmailVerified)
                .orElse(false);
    }
}