package com.artemk.schooltracking.dto;

import java.math.BigDecimal;
import java.util.List;

public record AllTimeStats(
        List<WeeklyStats> weeks,
        BigDecimal grandTotal,
        int totalGrades
) {
}
