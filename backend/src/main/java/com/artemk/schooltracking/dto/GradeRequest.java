package com.artemk.schooltracking.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record GradeRequest(
        @NotNull Long subjectId,
        @Min(2) @Max(5) int value,
        @NotNull LocalDate gradeDate
) {
}