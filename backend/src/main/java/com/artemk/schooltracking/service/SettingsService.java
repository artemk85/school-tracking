package com.artemk.schooltracking.service;

import com.artemk.schooltracking.domain.Settings;
import com.artemk.schooltracking.dto.SettingsDto;
import com.artemk.schooltracking.repository.SettingsRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SettingsService {

    private final SettingsRepository settingsRepository;

    public SettingsService(SettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    public Settings get(Long ownerId) {
        return settingsRepository.findByOwnerId(ownerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Настройки не найдены"));
    }

    public SettingsDto update(Long ownerId, SettingsDto incoming) {
        Settings settings = get(ownerId);
        incoming.applyTo(settings);
        return SettingsDto.from(settingsRepository.save(settings));
    }
}