package com.artemk.schooltracking.repository;

import com.artemk.schooltracking.domain.Settings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettingsRepository extends JpaRepository<Settings, Long> {
}