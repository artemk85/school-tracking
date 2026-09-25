package com.artemk.schooltracking.dto;

import jakarta.validation.constraints.NotBlank;

public record SubjectDto(
        Long id,
        @NotBlank String name,
        boolean core
) {
}