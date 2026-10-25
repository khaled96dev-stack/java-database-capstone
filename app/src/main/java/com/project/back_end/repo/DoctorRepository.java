package com.project.back_end.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.project.back_end.models.Doctor;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    // Returns the Doctor with the given email, or null if none exists.
    Doctor findByEmail(String email);

    // Partial (case-sensitive) match on the doctor's name.
    @Query("SELECT d FROM Doctor d WHERE d.name LIKE CONCAT('%', :name, '%')")
    List<Doctor> findByNameLike(@Param("name") String name);

    // Partial, case-insensitive name match plus exact, case-insensitive specialty.
    @Query("SELECT d FROM Doctor d WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :name, '%')) "
            + "AND LOWER(d.specialty) = LOWER(:specialty)")
    List<Doctor> findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(@Param("name") String name,
            @Param("specialty") String specialty);

    List<Doctor> findBySpecialtyIgnoreCase(String specialty);
}
