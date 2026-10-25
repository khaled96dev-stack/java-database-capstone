package com.project.back_end.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.back_end.DTO.AppointmentDTO;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.PatientRepository;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;

    public PatientService(PatientRepository patientRepository,
                          AppointmentRepository appointmentRepository,
                          TokenService tokenService) {
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
    }

    // Saves a new patient. Returns 1 on success, 0 on failure.
    public int createPatient(Patient patient) {
        try {
            patientRepository.save(patient);
            return 1;
        } catch (Exception e) {
            System.err.println("Error creating patient: " + e.getMessage());
            return 0;
        }
    }

    // Looks up the patient that owns the given token.
    public ResponseEntity<Map<String, Object>> getPatientDetails(String token) {
        Map<String, Object> response = new HashMap<>();
        try {
            Patient patient = patientRepository.findByEmail(tokenService.extractIdentifier(token));
            if (patient == null) {
                response.put("error", "Patient not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            response.put("patient", patient);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "Failed to fetch patient details");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Returns all of a patient's appointments. A patient token may only read its own appointments.
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getPatientAppointment(Long id, String token) {
        Map<String, Object> response = new HashMap<>();
        try {
            Patient patient = patientRepository.findByEmail(tokenService.extractIdentifier(token));
            if (patient != null && !patient.getId().equals(id)) {
                response.put("error", "Unauthorized");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            response.put("appointments", toDTOs(appointmentRepository.findByPatientId(id)));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "Failed to fetch appointments");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Filters by "past" (status 1) or "future" (status 0).
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> filterByCondition(String condition, Long id) {
        Map<String, Object> response = new HashMap<>();
        Integer status = statusFor(condition);
        if (status == null) {
            response.put("error", "Invalid condition. Use 'past' or 'future'");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        try {
            response.put("appointments",
                    toDTOs(appointmentRepository.findByPatient_IdAndStatusOrderByAppointmentTimeAsc(id, status)));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "Failed to filter appointments");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> filterByDoctor(String name, Long patientId) {
        Map<String, Object> response = new HashMap<>();
        try {
            response.put("appointments", toDTOs(appointmentRepository.filterByDoctorNameAndPatientId(name, patientId)));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "Failed to filter appointments");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> filterByDoctorAndCondition(String condition, String name, long patientId) {
        Map<String, Object> response = new HashMap<>();
        Integer status = statusFor(condition);
        if (status == null) {
            response.put("error", "Invalid condition. Use 'past' or 'future'");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        try {
            response.put("appointments",
                    toDTOs(appointmentRepository.filterByDoctorNameAndPatientIdAndStatus(name, patientId, status)));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "Failed to filter appointments");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    private static Integer statusFor(String condition) {
        if ("past".equalsIgnoreCase(condition)) {
            return 1;
        }
        if ("future".equalsIgnoreCase(condition)) {
            return 0;
        }
        return null;
    }

    private static List<AppointmentDTO> toDTOs(List<Appointment> appointments) {
        return appointments.stream().map(AppointmentService::toDTO).toList();
    }
}
