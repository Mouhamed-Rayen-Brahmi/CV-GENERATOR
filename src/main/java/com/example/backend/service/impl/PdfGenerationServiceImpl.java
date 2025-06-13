package com.example.backend.service.impl;

import com.example.backend.model.CV;
import com.example.backend.service.PdfGenerationService;
import com.lowagie.text.DocumentException;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

@Service
public class PdfGenerationServiceImpl implements PdfGenerationService {

    private static final Logger logger = LoggerFactory.getLogger(PdfGenerationServiceImpl.class);

    private final TemplateEngine templateEngine;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    public PdfGenerationServiceImpl(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    @Override
    public byte[] generatePdfFromCV(CV cv) {
        try {
            logger.info("Starting PDF generation for CV ID: {}", cv.getId());

            // Create Thymeleaf context
            Context context = new Context();
            context.setVariable("cv", cv);
            context.setVariable("uploadPath", uploadDir);
            
            // Add circular base64 encoded image if profile picture exists
            if (cv.getProfilePicture() != null && !cv.getProfilePicture().trim().isEmpty()) {
                String base64Image = getCircularImageAsBase64(cv.getProfilePicture());
                if (base64Image != null) {
                    context.setVariable("profileImageBase64", base64Image);
                    logger.info("Profile image successfully processed to circular format for CV ID: {}", cv.getId());
                } else {
                    logger.warn("Failed to process profile image to circular format for CV ID: {}", cv.getId());
                }
            } else {
                logger.info("No profile picture found for CV ID: {}", cv.getId());
            }

            // Process the HTML template
            String htmlContent = templateEngine.process("cv/pdf-template", context);
            logger.debug("HTML content generated, length: {}", htmlContent.length());

            // Write debug file if needed
            try {
                Files.write(Paths.get("debug-cv-template.html"), htmlContent.getBytes());
                logger.debug("Debug HTML file written for CV ID: {}", cv.getId());
            } catch (Exception e) {
                logger.warn("Could not write debug HTML file", e);
            }

            // Generate PDF
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ITextRenderer renderer = new ITextRenderer();
            
            // Set the HTML content
            renderer.setDocumentFromString(htmlContent);
            renderer.layout();
            renderer.createPDF(outputStream);

            byte[] pdfBytes = outputStream.toByteArray();
            logger.info("PDF generated successfully for CV ID: {}, size: {} bytes", cv.getId(), pdfBytes.length);

            return pdfBytes;

        } catch (DocumentException e) {
            logger.error("Document generation error for CV ID: {}", cv.getId(), e);
            throw new RuntimeException("Failed to generate PDF: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error during PDF generation for CV ID: {}", cv.getId(), e);
            throw new RuntimeException("Failed to generate PDF: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] generatePdfFromTemplate(String templateName, Object data) {
        try {
            logger.info("Starting PDF generation for template: {}", templateName);

            Context context = new Context();
            context.setVariable("data", data);
            context.setVariable("uploadPath", uploadDir);

            String htmlContent = templateEngine.process(templateName, context);
            logger.debug("HTML content generated for template: {}, length: {}", templateName, htmlContent.length());

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ITextRenderer renderer = new ITextRenderer();
            
            renderer.setDocumentFromString(htmlContent);
            renderer.layout();
            renderer.createPDF(outputStream);

            byte[] pdfBytes = outputStream.toByteArray();
            logger.info("PDF generated successfully for template: {}, size: {} bytes", templateName, pdfBytes.length);

            return pdfBytes;

        } catch (DocumentException e) {
            logger.error("Document generation error for template: {}", templateName, e);
            throw new RuntimeException("Failed to generate PDF: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error during PDF generation for template: {}", templateName, e);
            throw new RuntimeException("Failed to generate PDF: " + e.getMessage(), e);
        }
    }

    private String getCircularImageAsBase64(String filename) {
        try {
            logger.debug("Processing image to circular format: {}", filename);
            
            // Try different possible paths
            Path imagePath = findImagePath(filename);
            
            if (imagePath == null || !Files.exists(imagePath) || !Files.isRegularFile(imagePath)) {
                logger.error("Image file not found: {}", filename);
                return null;
            }

            // Read the original image
            BufferedImage originalImage = ImageIO.read(imagePath.toFile());
            if (originalImage == null) {
                logger.error("Could not read image file: {}", filename);
                return null;
            }

            // Create circular image
            BufferedImage circularImage = createCircularImage(originalImage, 100); // 100px diameter
            
            // Convert to base64
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(circularImage, "PNG", baos);
            byte[] imageBytes = baos.toByteArray();
            String base64 = Base64.getEncoder().encodeToString(imageBytes);
            
            logger.info("Successfully created circular image for: {}, size: {} bytes", filename, imageBytes.length);
            return "data:image/png;base64," + base64;

        } catch (IOException e) {
            logger.error("IO error processing image: {}", filename, e);
        } catch (Exception e) {
            logger.error("Unexpected error processing image: {}", filename, e);
        }
        return null;
    }

    private Path findImagePath(String filename) {
        // Try different possible paths
        Path[] possiblePaths = {
            Paths.get("uploads", "profile-pictures", filename),
            Paths.get(uploadDir, "profile-pictures", filename),
            Paths.get(uploadDir, filename),
            Paths.get(".", "uploads", "profile-pictures", filename),
            Paths.get(System.getProperty("user.dir"), "uploads", "profile-pictures", filename)
        };

        for (Path path : possiblePaths) {
            logger.debug("Checking path: {}", path.toAbsolutePath());
            if (Files.exists(path) && Files.isRegularFile(path)) {
                logger.debug("Found image at: {}", path.toAbsolutePath());
                return path;
            }
        }

        return null;
    }

    private BufferedImage createCircularImage(BufferedImage originalImage, int size) {
        // Resize image to square first
        BufferedImage squareImage;
        try {
            squareImage = Thumbnails.of(originalImage)
                    .size(size, size)
                    .crop(Positions.CENTER)
                    .asBufferedImage();
        } catch (IOException e) {
            // Fallback to manual resize if Thumbnailator fails
            squareImage = resizeImageManually(originalImage, size);
        }

        // Create circular mask
        BufferedImage circularImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = circularImage.createGraphics();
        
        // Enable anti-aliasing for smooth edges
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        
        // Create circular clip
        Ellipse2D.Double circle = new Ellipse2D.Double(0, 0, size, size);
        g2d.setClip(circle);
        
        // Draw the image within the circular clip
        g2d.drawImage(squareImage, 0, 0, size, size, null);
        
        g2d.dispose();
        
        return circularImage;
    }

    private BufferedImage resizeImageManually(BufferedImage originalImage, int size) {
        // Get the minimum dimension to create a square crop
        int minDimension = Math.min(originalImage.getWidth(), originalImage.getHeight());
        
        // Calculate crop coordinates to center the image
        int x = (originalImage.getWidth() - minDimension) / 2;
        int y = (originalImage.getHeight() - minDimension) / 2;
        
        // Crop to square
        BufferedImage croppedImage = originalImage.getSubimage(x, y, minDimension, minDimension);
        
        // Resize to target size
        BufferedImage resizedImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resizedImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(croppedImage, 0, 0, size, size, null);
        g2d.dispose();
        
        return resizedImage;
    }
}