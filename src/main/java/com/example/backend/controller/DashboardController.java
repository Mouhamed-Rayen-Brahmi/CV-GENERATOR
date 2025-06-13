package com.example.backend.controller;

import com.example.backend.model.User;
import com.example.backend.service.CVService;
import com.example.backend.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final UserService userService;
    private final CVService cvService;

    public DashboardController(UserService userService, CVService cvService) {
        this.userService = userService;
        this.cvService = cvService;
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails != null) {
            User user = userService.findByUsername(userDetails.getUsername());
            model.addAttribute("user", user);
            model.addAttribute("cvList", cvService.findAllByUser(user));
            model.addAttribute("recentCv", cvService.findMostRecentByUser(user));
        }
        return "dashboard/index";
    }
}