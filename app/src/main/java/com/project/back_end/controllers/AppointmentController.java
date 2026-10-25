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

import com.project.back_end.models.Appointment;
import com.project.back_end.services.AppointmentService;
import com.project.back_end.services.Service;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final Service service;

    public AppointmentController(AppointmentService appointmentService, Service service) {
        this.appointmentService = appointmentService;
        this.service = service;
    }

    // Doctor-only: appointments on a date, optionally filtered by patient name ("null" = no filter).
    @GetMapping("/{date}/{patientName}/{token}")
    public ResponseEntity<Map<String, Object>> getAppointments(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @PathVariable String patientName,
            @PathVariable String token) {
        Map<String, String> errors = service.validateToken(token, "doctor").getBody();
        if (errors != null && !errors.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new HashMap<>(errors));
        }
        return ResponseEntity.ok(appointmentService.getAppointment(patientName, date, token));
    }

    @PostMapping("/{token}")
    public ResponseEntity<Map<String, String>> bookAppointment(@Valid @RequestBody Appointment appointment,
            @PathVariable String token) {
        ResponseEntity<Map<String, String>> tokenCheck = service.validateToken(token, "patient");
        if (!tokenCheck.getStatusCode().is2xxSuccessful()) {
            return tokenCheck;
        }

        Map<String, String> response = new HashMap<>();
        int valid = service.validateAppointment(appointment);
        if (valid == -1) {
            response.put("message", "Invalid doctor id");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        if (valid == 0) {
            response.put("message", "Appointment already booked for given time or doctor not available");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        if (appointmentService.bookAppointment(appointment) == 1) {
            response.put("message", "Appointment booked successfully");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }
        response.put("message", "Internal server error");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @PutMapping("/{token}")
    public ResponseEntity<Map<String, String>> updateAppointment(@PathVariable String token,
            @Valid @RequestBody Appointment appointment) {
        ResponseEntity<Map<String, String>> tokenCheck = service.validateToken(token, "patient");
        if (!tokenCheck.getStatusCode().is2xxSuccessful()) {
            return tokenCheck;
        }
        return appointmentService.updateAppointment(appointment);
    }

    @DeleteMapping("/{id}/{token}")
    public ResponseEntity<Map<String, String>> cancelAppointment(@PathVariable long id, @PathVariable String token) {
        ResponseEntity<Map<String, String>> tokenCheck = service.validateToken(token, "patient");
        if (!tokenCheck.getStatusCode().is2xxSuccessful()) {
            return tokenCheck;
        }
        return appointmentService.cancelAppointment(id, token);
    }
}
