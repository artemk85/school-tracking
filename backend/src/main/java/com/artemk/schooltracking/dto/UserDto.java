package com.artemk.schooltracking.dto;

public record UserDto(
        Long id,
        String username,
        String displayName,
        String role,
        Long parentId
) {
}