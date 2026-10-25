package com.project.back_end.services;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import com.project.back_end.DTO.AppointmentDTO;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;

@org.springframework.stereotype.Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final Service service;
    private final TokenService tokenService;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              Service service,
                              TokenService tokenService,
                              PatientRepository patientRepository,
                              DoctorRepository doctorRepository) {
        this.appointmentRepository = appointmentRepository;
        this.service = service;
        this.tokenService = tokenService;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
    }

    // Saves a new appointment. Returns 1 on success, 0 on failure.
    @Transactional
    public int bookAppointment(Appointment appointment) {
        try {
            appointmentRepository.save(appointment);
            return 1;
        } catch (Exception e) {
            System.err.println("Error booking appointment: " + e.getMessage());
            return 0;
        }
    }

    // Updates an existing appointment after checking ownership and doctor availability.
    @Transactional
    public ResponseEntity<Map<String, String>> updateAppointment(Appointment appointment) {
        Map<String, String> response = new HashMap<>();
        try {
            Optional<Appointment> existingOpt = appointmentRepository.findById(appointment.getId());
            if (existingOpt.isEmpty()) {
                response.put("message", "Appointment not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            Appointment existing = existingOpt.get();

            if (appointment.getPatient() == null
                    || !existing.getPatient().getId().equals(appointment.getPatient().getId())) {
                response.put("message", "Patient ID does not match the appointment");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // Keeping the same doctor and time is always allowed; otherwise the new slot must be free.
            boolean sameSlot = appointment.getDoctor() != null
                    && existing.getDoctor().getId().equals(appointment.getDoctor().getId())
                    && existing.getAppointmentTime().equals(appointment.getAppointmentTime());
            if (!sameSlot) {
                int valid = service.validateAppointment(appointment);
                if (valid == -1) {
                    response.put("message", "Invalid doctor id");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                }
                if (valid == 0) {
                    response.put("message", "Appointment already booked for given time or doctor not available");
                    return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
                }
            }

            existing.setDoctor(appointment.getDoctor());
            existing.setAppointmentTime(appointment.getAppointmentTime());
            existing.setStatus(appointment.getStatus());
            appointmentRepository.save(existing);
            response.put("message", "Appointment updated successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error updating appointment: " + e.getMessage());
            response.put("message", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Deletes an appointment, but only if it belongs to the patient that owns the token.
    @Transactional
    public ResponseEntity<Map<String, String>> cancelAppointment(long id, String token) {
        Map<String, String> response = new HashMap<>();
        try {
            Optional<Appointment> appointmentOpt = appointmentRepository.findById(id);
            if (appointmentOpt.isEmpty()) {
                response.put("message", "Appointment not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            Appointment appointment = appointmentOpt.get();

            Patient patient = patientRepository.findByEmail(tokenService.extractIdentifier(token));
            if (patient == null || !patient.getId().equals(appointment.getPatient().getId())) {
                response.put("message", "You are not authorized to cancel this appointment");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            appointmentRepository.delete(appointment);
            response.put("message", "Appointment cancelled successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error cancelling appointment: " + e.getMessage());
            response.put("message", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Returns the token's doctor's appointments on the given date, optionally filtered by patient name.
    @Transactional(readOnly = true)
    public Map<String, Object> getAppointment(String pname, LocalDate date, String token) {
        Map<String, Object> result = new HashMap<>();
        Doctor doctor = doctorRepository.findByEmail(tokenService.extractIdentifier(token));
        if (doctor == null) {
            result.put("appointments", List.of());
            return result;
        }

        var start = date.atStartOfDay();
        var end = date.atTime(LocalTime.MAX);
        List<Appointment> appointments;
        if (pname == null || pname.isBlank() || pname.equalsIgnoreCase("null")) {
            appointments = appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(doctor.getId(), start, end);
        } else {
            appointments = appointmentRepository
                    .findByDoctorIdAndPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween(
                            doctor.getId(), pname, start, end);
        }

        result.put("appointments", appointments.stream().map(AppointmentService::toDTO).toList());
        return result;
    }

    // Sets the status of an appointment (e.g. 1 once a prescription has been written).
    @Transactional
    public void changeStatus(int status, long id) {
        appointmentRepository.updateStatus(status, id);
    }

    static AppointmentDTO toDTO(Appointment a) {
        Doctor d = a.getDoctor();
        Patient p = a.getPatient();
        return new AppointmentDTO(
                a.getId(),
                d.getId(),
                d.getName(),
                p.getId(),
                p.getName(),
                p.getEmail(),
                p.getPhone(),
                p.getAddress(),
                a.getAppointmentTime(),
                a.getStatus());
    }
}
