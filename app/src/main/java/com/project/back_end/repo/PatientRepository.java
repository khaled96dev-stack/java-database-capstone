package com.project.back_end.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.project.back_end.models.Patient;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    // Returns the Patient with the given email, or null if none exists.
    Patient findByEmail(String email);

    // Returns a Patient matching either the email or the phone number.
    Patient findByEmailOrPhone(String email, String phone);
}
