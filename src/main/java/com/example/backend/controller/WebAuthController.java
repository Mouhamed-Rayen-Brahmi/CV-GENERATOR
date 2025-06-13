package com.example.backend.controller;

import com.example.backend.dto.UserDto;
import com.example.backend.service.UserService;
import com.example.backend.service.VerificationService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class WebAuthController {

    private final UserService userService;
    private final VerificationService verificationService;

    public WebAuthController(UserService userService, VerificationService verificationService) {
        this.userService = userService;
        this.verificationService = verificationService;
    }

    @GetMapping("/login")
    public String loginForm(@RequestParam(value = "error", required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid email or password, or email not verified.");
        }
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("userDto", new UserDto());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("userDto") UserDto userDto, 
                          BindingResult result, 
                          Model model,
                          RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "auth/register";
        }
        
        try {
            userService.createUser(userDto);
            // Add the email to the redirect attributes so it's available on the verify page
            redirectAttributes.addFlashAttribute("email", userDto.getEmail());
            redirectAttributes.addFlashAttribute("message", 
                "Registration successful! Please check your email for a verification code.");
            return "redirect:/verify-email";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "auth/register";
        }
    }

    @GetMapping("/verify-email")
    public String verifyEmailForm(Model model) {
        return "auth/verify-email";
    }

    @GetMapping("/verify")
    public String verifyFromEmail(@RequestParam("token") String token, RedirectAttributes redirectAttributes) {
        if (verificationService.verifyEmail(token)) {
            redirectAttributes.addAttribute("success", true);
            return "redirect:/verify-email";
        } else {
            redirectAttributes.addAttribute("error", "Invalid or expired verification code.");
            return "redirect:/verify-email";
        }
    }

    @PostMapping("/verify")
    public String verifyEmail(@RequestParam("token") String token, 
                             @RequestParam(value = "email", required = false) String email,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        if (verificationService.verifyEmail(token)) {
            redirectAttributes.addAttribute("success", true);
            return "redirect:/verify-email";
        } else {
            model.addAttribute("error", "Invalid or expired verification code.");
            model.addAttribute("email", email);
            return "auth/verify-email";
        }
    }

    @PostMapping("/resend-verification")
    public String resendVerification(@RequestParam("email") String email, 
                                   RedirectAttributes redirectAttributes) {
        if (verificationService.resendVerificationCode(email)) {
            redirectAttributes.addFlashAttribute("resent", true);
            redirectAttributes.addFlashAttribute("email", email);
            return "redirect:/verify-email";
        } else {
            redirectAttributes.addFlashAttribute("error", "Unable to resend verification code. Please try again or contact support.");
            redirectAttributes.addFlashAttribute("email", email);
            return "redirect:/verify-email";
        }
    }
}