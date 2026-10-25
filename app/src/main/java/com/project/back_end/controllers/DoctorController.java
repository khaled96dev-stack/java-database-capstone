package com.project.back_end.controllers;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Doctor;
import com.project.back_end.services.DoctorService;
import com.project.back_end.services.Service;

import jakarta.validation.Valid;

@RestController
@RequestMapping("${api.path}" + "doctor")
public class DoctorController {

    private final DoctorService doctorService;
    private final Service service;

    public DoctorController(DoctorService doctorService, Service service) {
        this.doctorService = doctorService;
        this.service = service;
    }

    // Free time slots for a doctor on a date; the token must belong to the given user role.
    @GetMapping("/availability/{user}/{doctorId}/{date}/{token}")
    public ResponseEntity<Map<String, Object>> getDoctorAvailability(@PathVariable String user,
            @PathVariable Long doctorId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @PathVariable String token) {
        Map<String, String> errors = service.validateToken(token, user).getBody();
        if (errors != null && !errors.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new HashMap<>(errors));
        }
        Map<String, Object> response = new HashMap<>();
        response.put("availability", doctorService.getDoctorAvailability(doctorId, date));
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getDoctor() {
        Map<String, Object> response = new HashMap<>();
        response.put("doctors", doctorService.getDoctors());
        return ResponseEntity.ok(response);
    }

    // Admin-only: registers a new doctor.
    @PostMapping("/{token}")
    public ResponseEntity<Map<String, String>> saveDoctor(@Valid @RequestBody Doctor doctor,
            @PathVariable String token) {
        ResponseEntity<Map<String, String>> tokenCheck = service.validateToken(token, "admin");
        if (!tokenCheck.getStatusCode().is2xxSuccessful()) {
            return tokenCheck;
        }

        Map<String, String> response = new HashMap<>();
        switch (doctorService.saveDoctor(doctor)) {
            case 1:
                response.put("message", "Doctor added to db");
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            case -1:
                response.put("message", "Doctor already exists");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            default:
                response.put("message", "Some internal error occurred");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> doctorLogin(@RequestBody Login login) {
        return doctorService.validateDoctor(login);
    }

    // Admin-only: updates an existing doctor.
    @PutMapping("/{token}")
    public ResponseEntity<Map<String, String>> updateDoctor(@Valid @RequestBody Doctor doctor,
            @PathVariable String token) {
        ResponseEntity<Map<String, String>> tokenCheck = service.validateToken(token, "admin");
        if (!tokenCheck.getStatusCode().is2xxSuccessful()) {
            return tokenCheck;
        }

        Map<String, String> response = new HashMap<>();
        switch (doctorService.updateDoctor(doctor)) {
            case 1:
                response.put("message", "Doctor updated");
                return ResponseEntity.ok(response);
            case -1:
                response.put("message", "Doctor not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            default:
                response.put("message", "Some internal error occurred");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Admin-only: deletes a doctor and their appointments.
    @DeleteMapping("/{id}/{token}")
    public ResponseEntity<Map<String, String>> deleteDoctor(@PathVariable long id, @PathVariable String token) {
        ResponseEntity<Map<String, String>> tokenCheck = service.validateToken(token, "admin");
        if (!tokenCheck.getStatusCode().is2xxSuccessful()) {
            return tokenCheck;
        }

        Map<String, String> response = new HashMap<>();
        switch (doctorService.deleteDoctor(id)) {
            case 1:
                response.put("message", "Doctor deleted successfully");
                return ResponseEntity.ok(response);
            case -1:
                response.put("message", "Doctor not found with id " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            default:
                response.put("message", "Some internal error occurred");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Any segment may be the literal "null" to skip that filter.
    @GetMapping("/filter/{name}/{time}/{speciality}")
    public ResponseEntity<Map<String, Object>> filter(@PathVariable String name,
            @PathVariable String time,
            @PathVariable String speciality) {
        return ResponseEntity.ok(service.filterDoctor(name, speciality, time));
    }
}
