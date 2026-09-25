package com.artemk.schooltracking.service;

import com.artemk.schooltracking.domain.Settings;
import com.artemk.schooltracking.repository.SettingsRepository;
import org.springframework.stereotype.Service;

@Service
public class SettingsService {

    private static final Long SETTINGS_ID = 1L;

    private final SettingsRepository settingsRepository;

    public SettingsService(SettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    public Settings get() {
        return settingsRepository.findById(SETTINGS_ID)
                .orElseThrow(() -> new IllegalStateException("Настройки не инициализированы"));
    }

    public Settings update(Settings incoming) {
        Settings settings = get();
        settings.setFiveReward(incoming.getFiveReward());
        settings.setFourReward(incoming.getFourReward());
        settings.setThreePenalty(incoming.getThreePenalty());
        settings.setTwoPenalty(incoming.getTwoPenalty());
        settings.setCoreCoefficient(incoming.getCoreCoefficient());
        settings.setOtherCoefficient(incoming.getOtherCoefficient());
        return settingsRepository.save(settings);
    }
}