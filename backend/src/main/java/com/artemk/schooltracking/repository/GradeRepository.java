package com.artemk.schooltracking.repository;

import com.artemk.schooltracking.domain.Grade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface GradeRepository extends JpaRepository<Grade, Long> {
    List<Grade> findByOwnerIdAndChildIdAndGradeDateBetween(Long ownerId, Long childId, LocalDate from, LocalDate to);

    List<Grade> findByOwnerIdAndChildId(Long ownerId, Long childId);

    Optional<Grade> findByIdAndOwnerId(Long id, Long ownerId);

    boolean existsByIdAndOwnerId(Long id, Long ownerId);

    long countBySubjectId(Long subjectId);
}