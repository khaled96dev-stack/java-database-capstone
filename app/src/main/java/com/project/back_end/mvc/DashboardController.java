package com.project.back_end.mvc;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.project.back_end.services.Service;

@Controller
public class DashboardController {

    private static final String LOGIN_REDIRECT = "redirect:http://localhost:8080";

    @Autowired
    private Service service;

    // Renders the admin dashboard if the token belongs to an admin, otherwise redirects to login.
    @GetMapping("/adminDashboard/{token}")
    public String adminDashboard(@PathVariable String token) {
        Map<String, String> errors = service.validateToken(token, "admin").getBody();
        if (errors == null || !errors.isEmpty()) {
            return LOGIN_REDIRECT;
        }
        return "admin/adminDashboard";
    }

    // Renders the doctor dashboard if the token belongs to a doctor, otherwise redirects to login.
    @GetMapping("/doctorDashboard/{token}")
    public String doctorDashboard(@PathVariable String token) {
        Map<String, String> errors = service.validateToken(token, "doctor").getBody();
        if (errors == null || !errors.isEmpty()) {
            return LOGIN_REDIRECT;
        }
        return "doctor/doctorDashboard";
    }
}
