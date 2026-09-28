package com.artemk.schooltracking.repository;

import com.artemk.schooltracking.domain.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
    List<Subject> findByOwnerIdOrderByCoreDescNameAsc(Long ownerId);

    Optional<Subject> findByIdAndOwnerId(Long id, Long ownerId);

    Optional<Subject> findByOwnerIdAndNameIgnoreCase(Long ownerId, String name);

    boolean existsByOwnerIdAndNameIgnoreCase(Long ownerId, String name);

    boolean existsByIdAndOwnerId(Long id, Long ownerId);
}