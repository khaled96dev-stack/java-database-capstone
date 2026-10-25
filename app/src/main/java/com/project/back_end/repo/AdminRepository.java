package com.project.back_end.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.project.back_end.models.Admin;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {

    // Returns the Admin with the given username, or null if none exists.
    Admin findByUsername(String username);
}
