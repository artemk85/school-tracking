package com.artemk.schooltracking.repository;

import com.artemk.schooltracking.domain.Grade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface GradeRepository extends JpaRepository<Grade, Long> {
    List<Grade> findByGradeDateBetween(LocalDate from, LocalDate to);
}