package com.artemk.schooltracking.service;

import com.artemk.schooltracking.config.JwtService;
import com.artemk.schooltracking.domain.Role;
import com.artemk.schooltracking.domain.Settings;
import com.artemk.schooltracking.domain.Subject;
import com.artemk.schooltracking.domain.User;
import com.artemk.schooltracking.dto.*;
import com.artemk.schooltracking.repository.SettingsRepository;
import com.artemk.schooltracking.repository.SubjectRepository;
import com.artemk.schooltracking.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;
    private final SettingsRepository settingsRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       SubjectRepository subjectRepository,
                       SettingsRepository settingsRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.subjectRepository = subjectRepository;
        this.settingsRepository = settingsRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String username = request.username().trim();
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Имя пользователя уже занято");
        }
        User parent = userRepository.save(new User(
                username,
                passwordEncoder.encode(request.password()),
                displayNameOr(request.displayName(), username),
                Role.PARENT,
                null
        ));
        seedDefaults(parent);
        log.info("Зарегистрирован родитель {}", username);
        return buildResponse(parent);
    }

    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    request.username().trim(), request.password()));
        } catch (BadCredentialsException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неверный логин или пароль");
        }
        User user = userRepository.findByUsernameIgnoreCase(request.username().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неверный логин или пароль"));
        return buildResponse(user);
    }

    public UserDto current(User user) {
        return toDto(user);
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не найден"));
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Неверный текущий пароль");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        log.info("Пользователь {} сменил пароль", user.getUsername());
    }

    @Transactional
    public UserDto createChild(Long parentId, CreateChildRequest request) {
        String username = request.username().trim();
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Имя пользователя уже занято");
        }
        User parent = userRepository.findById(parentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Родитель не найден"));
        User child = userRepository.save(new User(
                username,
                passwordEncoder.encode(request.password()),
                displayNameOr(request.displayName(), username),
                Role.CHILD,
                parent
        ));
        log.info("Родитель {} создал ребёнка {}", parent.getUsername(), username);
        return toDto(child);
    }

    public List<UserDto> children(Long parentId) {
        return userRepository.findByParent_IdOrderByUsernameAsc(parentId).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public void deleteChild(Long parentId, Long childId) {
        User child = userRepository.findByIdAndParent_Id(childId, parentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ребёнок не найден"));
        userRepository.delete(child);
    }

    public UserDto toDto(User user) {
        return new UserDto(user.getId(), user.getUsername(), user.getDisplayName(),
                user.getRole().name(), user.getParentId());
    }

    private AuthResponse buildResponse(User user) {
        String token = jwtService.generateToken(user.getId(), user.getUsername(), user.getRole().name());
        return new AuthResponse(token, toDto(user));
    }

    private String displayNameOr(String displayName, String fallback) {
        return (displayName == null || displayName.isBlank()) ? fallback : displayName.trim();
    }

    private void seedDefaults(User owner) {
        settingsRepository.save(new Settings(owner));
        List<Subject> defaults = List.of(
                new Subject(owner, "Математика", true),
                new Subject(owner, "Русский язык", true),
                new Subject(owner, "Литература", false),
                new Subject(owner, "История", false),
                new Subject(owner, "Биология", false),
                new Subject(owner, "География", false),
                new Subject(owner, "Физика", false),
                new Subject(owner, "Химия", false),
                new Subject(owner, "Английский язык", false),
                new Subject(owner, "Информатика", false),
                new Subject(owner, "Окружающий мир", false),
                new Subject(owner, "Физкультура", false)
        );
        subjectRepository.saveAll(defaults);
    }
}