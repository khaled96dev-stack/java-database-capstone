package com.project.back_end.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.project.back_end.models.Prescription;
import com.project.back_end.repo.PrescriptionRepository;

@Service
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;

    public PrescriptionService(PrescriptionRepository prescriptionRepository) {
        this.prescriptionRepository = prescriptionRepository;
    }

    // Saves a prescription unless one already exists for the same appointment.
    public ResponseEntity<Map<String, String>> savePrescription(Prescription prescription) {
        Map<String, String> response = new HashMap<>();
        try {
            if (!prescriptionRepository.findByAppointmentId(prescription.getAppointmentId()).isEmpty()) {
                response.put("message", "Prescription already exists for this appointment");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            prescriptionRepository.save(prescription);
            response.put("message", "Prescription saved");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            System.err.println("Error saving prescription: " + e.getMessage());
            response.put("message", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Returns the prescriptions for an appointment under the "prescription" key.
    public ResponseEntity<Map<String, Object>> getPrescription(Long appointmentId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Prescription> prescriptions = prescriptionRepository.findByAppointmentId(appointmentId);
            if (prescriptions.isEmpty()) {
                response.put("message", "No prescription found for this appointment");
            }
            response.put("prescription", prescriptions);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error fetching prescription: " + e.getMessage());
            response.put("message", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
