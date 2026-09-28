package com.artemk.schooltracking.web;

import com.artemk.schooltracking.dto.CreateChildRequest;
import com.artemk.schooltracking.dto.UserDto;
import com.artemk.schooltracking.service.AuthService;
import com.artemk.schooltracking.service.CurrentUser;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/children")
public class ChildrenController {

    private final AuthService authService;
    private final CurrentUser currentUser;

    public ChildrenController(AuthService authService, CurrentUser currentUser) {
        this.authService = authService;
        this.currentUser = currentUser;
    }

    @GetMapping
    @PreAuthorize("hasRole('PARENT')")
    public List<UserDto> children() {
        return authService.children(currentUser.userId());
    }

    @PostMapping
    @PreAuthorize("hasRole('PARENT')")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@Valid @RequestBody CreateChildRequest request) {
        log.debug("POST /api/children — {}", request.username());
        return authService.createChild(currentUser.userId(), request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PARENT')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        authService.deleteChild(currentUser.userId(), id);
    }
}