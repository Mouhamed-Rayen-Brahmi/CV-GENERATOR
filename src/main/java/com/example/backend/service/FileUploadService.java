package com.example.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileUploadService {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadService.class);
    private static final String UPLOAD_DIR = "uploads/profile-pictures/";
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final String[] ALLOWED_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif"};

    public String uploadProfilePicture(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        // Validate file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IOException("File size too large. Maximum allowed size is 5MB.");
        }

        // Validate file extension
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IOException("Invalid file name.");
        }

        String extension = getFileExtension(originalFilename).toLowerCase();
        boolean isValidExtension = false;
        for (String allowedExt : ALLOWED_EXTENSIONS) {
            if (extension.equals(allowedExt)) {
                isValidExtension = true;
                break;
            }
        }

        if (!isValidExtension) {
            throw new IOException("Invalid file type. Only JPG, JPEG, PNG, and GIF files are allowed.");
        }

        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            logger.info("Created upload directory: {}", uploadPath.toAbsolutePath());
        }

        // Generate unique filename
        String filename = UUID.randomUUID().toString() + extension;
        Path filePath = uploadPath.resolve(filename);

        // Copy file to upload directory
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        logger.info("Profile picture uploaded successfully to: {}", filePath.toAbsolutePath());
        
        // Log the final filename being returned
        logger.info("Returning filename: {}", filename);
        
        return filename;
    }

    public void deleteProfilePicture(String filename) {
        if (filename == null || filename.isEmpty()) {
            return;
        }

        try {
            Path filePath = Paths.get(UPLOAD_DIR).resolve(filename);
            Files.deleteIfExists(filePath);
            logger.info("Profile picture deleted successfully: {}", filename);
        } catch (IOException e) {
            logger.error("Error deleting profile picture: {}", filename, e);
        }
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex);
    }

    @PostConstruct
    public void init() {
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                logger.info("Created upload directory: {}", uploadPath.toAbsolutePath());
            } else {
                logger.info("Upload directory already exists: {}", uploadPath.toAbsolutePath());
            }
            
            // List existing files for debugging
            if (Files.exists(uploadPath)) {
                logger.info("Contents of upload directory:");
                Files.list(uploadPath).forEach(path -> 
                    logger.info("  - {}", path.getFileName()));
            }
        } catch (IOException e) {
            logger.error("Could not create upload directory: {}", UPLOAD_DIR, e);
            throw new RuntimeException("Could not create upload directory", e);
        }
    }
}