package com.artemk.schooltracking.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record GradeDto(
        Long id,
        Long subjectId,
        String subjectName,
        boolean core,
        int value,
        LocalDate gradeDate,
        BigDecimal coefficient,
        BigDecimal amount
) {
}