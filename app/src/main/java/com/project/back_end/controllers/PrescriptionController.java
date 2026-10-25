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

import com.project.back_end.models.Prescription;
import com.project.back_end.services.AppointmentService;
import com.project.back_end.services.PrescriptionService;
import com.project.back_end.services.Service;

import jakarta.validation.Valid;

@RestController
@RequestMapping("${api.path}" + "prescription")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;
    private final Service service;
    private final AppointmentService appointmentService;

    public PrescriptionController(PrescriptionService prescriptionService,
                                  Service service,
                                  AppointmentService appointmentService) {
        this.prescriptionService = prescriptionService;
        this.service = service;
        this.appointmentService = appointmentService;
    }

    // Doctor-only: saves a prescription and marks the appointment as completed (status 1).
    @PostMapping("/{token}")
    public ResponseEntity<Map<String, String>> savePrescription(@PathVariable String token,
            @Valid @RequestBody Prescription prescription) {
        ResponseEntity<Map<String, String>> tokenCheck = service.validateToken(token, "doctor");
        if (!tokenCheck.getStatusCode().is2xxSuccessful()) {
            return tokenCheck;
        }

        ResponseEntity<Map<String, String>> result = prescriptionService.savePrescription(prescription);
        if (result.getStatusCode().is2xxSuccessful()) {
            appointmentService.changeStatus(1, prescription.getAppointmentId());
        }
        return result;
    }

    // Doctor-only: returns {"prescription": [...]} for the appointment.
    @GetMapping("/{appointmentId}/{token}")
    public ResponseEntity<Map<String, Object>> getPrescription(@PathVariable Long appointmentId,
            @PathVariable String token) {
        Map<String, String> errors = service.validateToken(token, "doctor").getBody();
        if (errors != null && !errors.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new HashMap<>(errors));
        }
        return prescriptionService.getPrescription(appointmentId);
    }
}
