package com.smarthfashion.admin.repository;

import com.smarthfashion.admin.domain.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    List<AppUser> findByEmailContainingIgnoreCaseOrNombreContainingIgnoreCaseOrApellidoContainingIgnoreCase(String email, String nombre, String apellido);

    // Sorted variants to show newest first
    List<AppUser> findAllByOrderByFechaRegistroDesc();

    List<AppUser> findByEmailContainingIgnoreCaseOrNombreContainingIgnoreCaseOrApellidoContainingIgnoreCaseOrderByFechaRegistroDesc(
            String email, String nombre, String apellido);
}
