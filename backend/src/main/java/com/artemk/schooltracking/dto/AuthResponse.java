package com.artemk.schooltracking.dto;

public record AuthResponse(
        String token,
        UserDto user
) {
}