package com.project.back_end.services;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Admin;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;

@org.springframework.stereotype.Service
public class Service {

    private final TokenService tokenService;
    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final DoctorService doctorService;
    private final PatientService patientService;

    public Service(TokenService tokenService,
                   AdminRepository adminRepository,
                   DoctorRepository doctorRepository,
                   PatientRepository patientRepository,
                   DoctorService doctorService,
                   PatientService patientService) {
        this.tokenService = tokenService;
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.doctorService = doctorService;
        this.patientService = patientService;
    }

    // Returns an empty body with 200 OK when the token is valid for the given role,
    // otherwise a 401 Unauthorized with an "error" message.
    public ResponseEntity<Map<String, String>> validateToken(String token, String user) {
        Map<String, String> response = new HashMap<>();
        if (!tokenService.validateToken(token, user)) {
            response.put("error", "Invalid or expired token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        return ResponseEntity.ok(response);
    }

    // Checks admin credentials and returns a JWT (subject = username) on success.
    public ResponseEntity<Map<String, String>> validateAdmin(Admin receivedAdmin) {
        Map<String, String> response = new HashMap<>();
        try {
            Admin admin = adminRepository.findByUsername(receivedAdmin.getUsername());
            if (admin == null || !admin.getPassword().equals(receivedAdmin.getPassword())) {
                response.put("error", "Invalid username or password");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            response.put("token", tokenService.generateToken(admin.getUsername()));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Filters doctors by any combination of name, specialty and AM/PM time; no filters returns all doctors.
    public Map<String, Object> filterDoctor(String name, String specialty, String time) {
        boolean hasName = isPresent(name);
        boolean hasSpecialty = isPresent(specialty);
        boolean hasTime = isPresent(time);

        if (hasName && hasSpecialty && hasTime) {
            return doctorService.filterDoctorsByNameSpecilityandTime(name, specialty, time);
        }
        if (hasName && hasTime) {
            return doctorService.filterDoctorByNameAndTime(name, time);
        }
        if (hasName && hasSpecialty) {
            return doctorService.filterDoctorByNameAndSpecility(name, specialty);
        }
        if (hasSpecialty && hasTime) {
            return doctorService.filterDoctorByTimeAndSpecility(specialty, time);
        }
        if (hasName) {
            return doctorService.findDoctorByName(name);
        }
        if (hasSpecialty) {
            return doctorService.filterDoctorBySpecility(specialty);
        }
        if (hasTime) {
            return doctorService.filterDoctorsByTime(time);
        }
        Map<String, Object> response = new HashMap<>();
        response.put("doctors", doctorService.getDoctors());
        return response;
    }

    // Returns 1 if the requested time matches a free slot, 0 if unavailable, -1 if the doctor doesn't exist.
    public int validateAppointment(Appointment appointment) {
        if (appointment.getDoctor() == null || appointment.getDoctor().getId() == null
                || doctorRepository.findById(appointment.getDoctor().getId()).isEmpty()) {
            return -1;
        }
        List<String> slots = doctorService.getDoctorAvailability(
                appointment.getDoctor().getId(), appointment.getAppointmentTime().toLocalDate());
        String requested = appointment.getAppointmentTime().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"));
        boolean available = slots.stream().anyMatch(slot -> slot.split("-")[0].trim().equals(requested));
        return available ? 1 : 0;
    }

    // Returns false if a patient with the same email or phone already exists.
    public boolean validatePatient(Patient patient) {
        return patientRepository.findByEmailOrPhone(patient.getEmail(), patient.getPhone()) == null;
    }

    // Checks patient credentials and returns a JWT on success.
    public ResponseEntity<Map<String, String>> validatePatientLogin(Login login) {
        Map<String, String> response = new HashMap<>();
        try {
            Patient patient = patientRepository.findByEmail(login.getIdentifier());
            if (patient == null || !patient.getPassword().equals(login.getPassword())) {
                response.put("error", "Invalid email or password");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            response.put("token", tokenService.generateToken(patient.getEmail()));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Filters the token's patient's appointments by condition (past/future) and/or doctor name.
    public ResponseEntity<Map<String, Object>> filterPatient(String condition, String name, String token) {
        Patient patient = patientRepository.findByEmail(tokenService.extractIdentifier(token));
        if (patient == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Patient not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        boolean hasCondition = isPresent(condition);
        boolean hasName = isPresent(name);
        if (hasCondition && hasName) {
            return patientService.filterByDoctorAndCondition(condition, name, patient.getId());
        }
        if (hasCondition) {
            return patientService.filterByCondition(condition, patient.getId());
        }
        if (hasName) {
            return patientService.filterByDoctor(name, patient.getId());
        }
        return patientService.getPatientAppointment(patient.getId(), token);
    }

    // The frontend sends the literal string "null" for empty path filters.
    private static boolean isPresent(String value) {
        return value != null && !value.isBlank() && !value.equalsIgnoreCase("null");
    }
}
