package com.artemk.schooltracking.web;

import com.artemk.schooltracking.domain.User;
import com.artemk.schooltracking.dto.AuthResponse;
import com.artemk.schooltracking.dto.LoginRequest;
import com.artemk.schooltracking.dto.RegisterRequest;
import com.artemk.schooltracking.dto.UserDto;
import com.artemk.schooltracking.repository.UserRepository;
import com.artemk.schooltracking.service.AuthService;
import com.artemk.schooltracking.service.CurrentUser;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final CurrentUser currentUser;
    private final UserRepository userRepository;

    public AuthController(AuthService authService, CurrentUser currentUser, UserRepository userRepository) {
        this.authService = authService;
        this.currentUser = currentUser;
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        log.debug("POST /api/auth/register — {}", request.username());
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        log.debug("POST /api/auth/login — {}", request.username());
        return authService.login(request);
    }

    @GetMapping("/me")
    public UserDto me() {
        User user = userRepository.findById(currentUser.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не найден"));
        return authService.toDto(user);
    }
}