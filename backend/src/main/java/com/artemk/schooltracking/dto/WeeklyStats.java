package com.artemk.schooltracking.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record WeeklyStats(
        LocalDate weekStart,
        LocalDate weekEnd,
        int gradeCount,
        BigDecimal total
) {
}
