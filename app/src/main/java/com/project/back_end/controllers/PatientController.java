package com.project.back_end.controllers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Patient;
import com.project.back_end.services.PatientService;
import com.project.back_end.services.Service;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/patient")
public class PatientController {

    private final PatientService patientService;
    private final Service service;

    public PatientController(PatientService patientService, Service service) {
        this.patientService = patientService;
        this.service = service;
    }

    // Returns the logged-in patient's details.
    @GetMapping("/{token}")
    public ResponseEntity<Map<String, Object>> getPatient(@PathVariable String token) {
        Map<String, String> errors = service.validateToken(token, "patient").getBody();
        if (errors != null && !errors.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new HashMap<>(errors));
        }
        return patientService.getPatientDetails(token);
    }

    // Registers a new patient. The frontend shows the "message" field to the user.
    @PostMapping
    public ResponseEntity<Map<String, String>> createPatient(@Valid @RequestBody Patient patient) {
        Map<String, String> response = new HashMap<>();
        if (!service.validatePatient(patient)) {
            response.put("message", "Patient with this email or phone number already exists");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }
        if (patientService.createPatient(patient) == 1) {
            response.put("message", "Signup successful");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }
        response.put("message", "Internal server error");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Login login) {
        return service.validatePatientLogin(login);
    }


    // Appointments for a patient. The frontend adds the caller's role ("patient" or "doctor");
    // without it the token must belong to a patient.
    @GetMapping({"/{id}/{token}", "/{id}/{user}/{token}"})
    public ResponseEntity<Map<String, Object>> getPatientAppointment(@PathVariable Long id,
            @PathVariable(required = false) String user,
            @PathVariable String token) {
        String role = (user == null) ? "patient" : user;
        Map<String, String> errors = service.validateToken(token, role).getBody();
        if (errors != null && !errors.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new HashMap<>(errors));
        }
        return patientService.getPatientAppointment(id, token);
    }

    // condition is "past"/"future" and name is a doctor name; either may be the literal "null".
    @GetMapping("/filter/{condition}/{name}/{token}")
    public ResponseEntity<Map<String, Object>> filterPatientAppointment(@PathVariable String condition,
            @PathVariable String name,
            @PathVariable String token) {
        Map<String, String> errors = service.validateToken(token, "patient").getBody();
        if (errors != null && !errors.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new HashMap<>(errors));
        }
        return service.filterPatient(condition, name, token);
    }
}
