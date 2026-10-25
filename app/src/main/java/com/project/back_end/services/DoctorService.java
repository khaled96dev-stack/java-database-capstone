package com.project.back_end.services;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;

@org.springframework.stereotype.Service
public class DoctorService {

    private static final DateTimeFormatter HH_MM = DateTimeFormatter.ofPattern("HH:mm");

    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;

    public DoctorService(DoctorRepository doctorRepository,
                         AppointmentRepository appointmentRepository,
                         TokenService tokenService) {
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
    }

    // Returns the doctor's time slots (e.g. "09:00-10:00") on the given date that are not yet booked.
    @Transactional(readOnly = true)
    public List<String> getDoctorAvailability(Long doctorId, LocalDate date) {
        Doctor doctor = doctorRepository.findById(doctorId).orElse(null);
        if (doctor == null || doctor.getAvailableTimes() == null) {
            return List.of();
        }

        List<Appointment> appointments = appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(
                doctorId, date.atStartOfDay(), date.atTime(LocalTime.MAX));
        Set<String> booked = appointments.stream()
                .map(a -> a.getAppointmentTime().toLocalTime().format(HH_MM))
                .collect(Collectors.toSet());

        return doctor.getAvailableTimes().stream()
                .filter(slot -> !booked.contains(slotStart(slot)))
                .toList();
    }

    // Returns 1 on success, -1 if a doctor with the same email exists, 0 on error.
    public int saveDoctor(Doctor doctor) {
        try {
            if (doctorRepository.findByEmail(doctor.getEmail()) != null) {
                return -1;
            }
            doctorRepository.save(doctor);
            return 1;
        } catch (Exception e) {
            System.err.println("Error saving doctor: " + e.getMessage());
            return 0;
        }
    }

    // Returns 1 on success, -1 if the doctor does not exist, 0 on error.
    public int updateDoctor(Doctor doctor) {
        try {
            if (doctor.getId() == null || !doctorRepository.existsById(doctor.getId())) {
                return -1;
            }
            doctorRepository.save(doctor);
            return 1;
        } catch (Exception e) {
            System.err.println("Error updating doctor: " + e.getMessage());
            return 0;
        }
    }

    @Transactional(readOnly = true)
    public List<Doctor> getDoctors() {
        return loadTimes(doctorRepository.findAll());
    }

    // Deletes the doctor and all of their appointments. Returns 1, -1 (not found) or 0 (error).
    @Transactional
    public int deleteDoctor(long id) {
        try {
            if (!doctorRepository.existsById(id)) {
                return -1;
            }
            appointmentRepository.deleteAllByDoctorId(id);
            doctorRepository.deleteById(id);
            return 1;
        } catch (Exception e) {
            System.err.println("Error deleting doctor: " + e.getMessage());
            return 0;
        }
    }

    // Checks doctor credentials and returns a JWT on success.
    public ResponseEntity<Map<String, String>> validateDoctor(Login login) {
        Map<String, String> response = new HashMap<>();
        try {
            Doctor doctor = doctorRepository.findByEmail(login.getIdentifier());
            if (doctor == null || !doctor.getPassword().equals(login.getPassword())) {
                response.put("error", "Invalid email or password");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            response.put("token", tokenService.generateToken(doctor.getEmail()));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Transactional(readOnly = true)
    public Map<String, Object> findDoctorByName(String name) {
        return doctorsResponse(doctorRepository.findByNameLike(name));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> filterDoctorsByNameSpecilityandTime(String name, String specialty, String amOrPm) {
        List<Doctor> doctors = doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, specialty);
        return doctorsResponse(filterDoctorByTime(doctors, amOrPm));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> filterDoctorByNameAndTime(String name, String amOrPm) {
        List<Doctor> doctors = doctorRepository.findByNameLike(name);
        return doctorsResponse(filterDoctorByTime(doctors, amOrPm));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> filterDoctorByNameAndSpecility(String name, String specilty) {
        return doctorsResponse(doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, specilty));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> filterDoctorByTimeAndSpecility(String specilty, String amOrPm) {
        List<Doctor> doctors = doctorRepository.findBySpecialtyIgnoreCase(specilty);
        return doctorsResponse(filterDoctorByTime(doctors, amOrPm));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> filterDoctorBySpecility(String specilty) {
        return doctorsResponse(doctorRepository.findBySpecialtyIgnoreCase(specilty));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> filterDoctorsByTime(String amOrPm) {
        return doctorsResponse(filterDoctorByTime(doctorRepository.findAll(), amOrPm));
    }

    // Keeps doctors with at least one slot starting in the morning (AM) or from noon onward (PM).
    private List<Doctor> filterDoctorByTime(List<Doctor> doctors, String amOrPm) {
        boolean wantAm = "AM".equalsIgnoreCase(amOrPm);
        return doctors.stream()
                .filter(d -> d.getAvailableTimes() != null && d.getAvailableTimes().stream()
                        .anyMatch(slot -> {
                            int hour = LocalTime.parse(slotStart(slot), HH_MM).getHour();
                            return wantAm ? hour < 12 : hour >= 12;
                        }))
                .toList();
    }

    // "09:00-10:00" -> "09:00"
    private static String slotStart(String slot) {
        return slot.split("-")[0].trim();
    }

    private Map<String, Object> doctorsResponse(List<Doctor> doctors) {
        Map<String, Object> response = new HashMap<>();
        response.put("doctors", loadTimes(doctors));
        return response;
    }

    // availableTimes is lazily loaded; touch it inside the transaction so it can be serialized later.
    private static List<Doctor> loadTimes(List<Doctor> doctors) {
        doctors.forEach(d -> {
            if (d.getAvailableTimes() != null) {
                d.getAvailableTimes().size();
            }
        });
        return doctors;
    }
}
