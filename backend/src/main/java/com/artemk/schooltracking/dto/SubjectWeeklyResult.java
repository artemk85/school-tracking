package com.artemk.schooltracking.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record SubjectWeeklyResult(
        Long subjectId,
        String subjectName,
        boolean core,
        BigDecimal coefficient,
        List<GradeDto> grades,
        BigDecimal amount
) {
}