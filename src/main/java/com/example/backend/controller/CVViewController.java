package com.example.backend.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.backend.dto.CVDto;
import com.example.backend.model.CV;
import com.example.backend.model.User;
import com.example.backend.service.CVService;
import com.example.backend.service.EmailService;
import com.example.backend.service.FileUploadService;
import com.example.backend.service.PdfGenerationService;
import com.example.backend.service.UserService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/cv")
public class CVViewController {

    private final CVService cvService;
    private final UserService userService;
    private final PdfGenerationService pdfService; // This should now inject the implementation
    private final EmailService emailService;
    private final FileUploadService fileUploadService;

    private static final Logger logger = LoggerFactory.getLogger(CVViewController.class);

    @Autowired
    private Environment environment;

    public CVViewController(CVService cvService, UserService userService, 
                           PdfGenerationService pdfService, EmailService emailService,
                           FileUploadService fileUploadService) {
        this.cvService = cvService;
        this.userService = userService;
        this.pdfService = pdfService;
        this.emailService = emailService;
        this.fileUploadService = fileUploadService;
    }

    @GetMapping("/create")
    public String createCVForm(Model model) {
        model.addAttribute("cvDto", new CVDto());
        return "cv/create";
    }

    @PostMapping("/create")
    public String createCV(@Valid @ModelAttribute("cvDto") CVDto cvDto,
                          BindingResult result,
                          @AuthenticationPrincipal UserDetails userDetails,
                          Model model) {
        if (result.hasErrors()) {
            return "cv/create";
        }
        
        try {
            // Handle profile picture upload
            if (cvDto.getProfilePictureFile() != null && !cvDto.getProfilePictureFile().isEmpty()) {
                String filename = fileUploadService.uploadProfilePicture(cvDto.getProfilePictureFile());
                cvDto.setProfilePicture(filename);
            }
            
            User user = userService.findByUsername(userDetails.getUsername());
            CV createdCV = cvService.createCV(cvDto, user);
            
            // Send email notification about CV creation
            try {
                emailService.sendCVCreatedNotification(
                    user.getEmail(), 
                    user.getFirstName(), 
                    createdCV.getTitle(), 
                    createdCV.getId()
                );
            } catch (Exception e) {
                logger.warn("Failed to send CV creation notification email", e);
                // Don't fail the CV creation if email fails
            }
            
            return "redirect:/dashboard";
        } catch (Exception e) {
            logger.error("Error creating CV", e);
            model.addAttribute("error", "Error uploading file: " + e.getMessage());
            return "cv/create";
        }
    }

    @GetMapping("/{id}")
    public String viewCV(@PathVariable Long id, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            CV cv = cvService.findById(id);
            User user = userService.findByUsername(userDetails.getUsername());
            
            if (!cv.getUser().getId().equals(user.getId())) {
                return "redirect:/dashboard";
            }
            
            model.addAttribute("cv", cv);
            return "cv/view";
        } catch (Exception e) {
            logger.error("Error viewing CV", e);
            return "redirect:/dashboard";
        }
    }

    @GetMapping("/{id}/edit")
    public String editCVForm(@PathVariable Long id, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            CV cv = cvService.findById(id);
            User user = userService.findByUsername(userDetails.getUsername());
            
            if (!cv.getUser().getId().equals(user.getId())) {
                return "redirect:/dashboard";
            }
            
            CVDto cvDto = cvService.convertToDto(cv);
            model.addAttribute("cvDto", cvDto);
            return "cv/edit";
        } catch (Exception e) {
            logger.error("Error loading CV for edit", e);
            return "redirect:/dashboard";
        }
    }

    @PostMapping("/{id}/edit")
    public String updateCV(@PathVariable Long id,
                          @Valid @ModelAttribute("cvDto") CVDto cvDto,
                          BindingResult result,
                          @AuthenticationPrincipal UserDetails userDetails,
                          Model model) {
        if (result.hasErrors()) {
            return "cv/edit";
        }
        
        try {
            // Handle profile picture upload
            if (cvDto.getProfilePictureFile() != null && !cvDto.getProfilePictureFile().isEmpty()) {
                String filename = fileUploadService.uploadProfilePicture(cvDto.getProfilePictureFile());
                cvDto.setProfilePicture(filename);
            }
            
            User user = userService.findByUsername(userDetails.getUsername());
            cvService.updateCV(id, cvDto, user);
            return "redirect:/cv/" + id;
        } catch (Exception e) {
            logger.error("Error updating CV", e);
            model.addAttribute("error", "Error uploading file: " + e.getMessage());
            return "cv/edit";
        }
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<?> downloadPdf(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            logger.info("PDF download requested for CV ID: {}", id);
            
            // Get the CV and check permissions
            CV cv = cvService.findById(id);
            User user = userService.findByUsername(userDetails.getUsername());
            
            if (!cv.getUser().getId().equals(user.getId())) {
                logger.warn("Unauthorized access attempt to CV ID: {} by user: {}", id, userDetails.getUsername());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            byte[] pdfContent = pdfService.generatePdfFromCV(cv);
            
            if (pdfContent == null || pdfContent.length == 0) {
                logger.error("Generated PDF content is empty for CV ID: {}", id);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("Generated PDF is empty");
            }
            
            logger.info("PDF generated successfully for CV ID: {}, size: {} bytes", id, pdfContent.length);
            
            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"cv-" + cv.getId() + ".pdf\"")
                .body(pdfContent);
        } catch (Exception e) {
            logger.error("Error generating PDF for CV ID: {}", id, e);
            
            // Return error page or error data based on accept header
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.TEXT_HTML)
                .body("<html><body><h1>Error Generating PDF</h1>" +
                      "<p>An error occurred while generating your PDF: " + e.getMessage() + "</p>" +
                      "<p><a href='/dashboard'>Return to Dashboard</a></p></body></html>");
        }
    }

    @GetMapping("/{id}/debug-template")
    public String debugPdfTemplate(@PathVariable Long id, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        // Only allow in development mode
        if (!environment.acceptsProfiles(Profiles.of("dev"))) {
            return "redirect:/dashboard";
        }
        
        try {
            // Get the CV and check permissions
            CV cv = cvService.findById(id);
            User user = userService.findByUsername(userDetails.getUsername());
            
            if (!cv.getUser().getId().equals(user.getId())) {
                return "redirect:/dashboard";
            }
            
            // Add CV to model to render the template directly
            model.addAttribute("cv", cv);
            
            // Render the PDF template directly in the browser for debugging
            return "cv/pdf-template";
        } catch (Exception e) {
            logger.error("Error rendering debug template for CV ID: {}", id, e);
            return "redirect:/dashboard";
        }
    }
}