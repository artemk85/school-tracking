package com.artemk.schooltracking.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record WeeklyReport(
        LocalDate weekStart,
        LocalDate weekEnd,
        BigDecimal total,
        List<SubjectWeeklyResult> subjects
) {
}