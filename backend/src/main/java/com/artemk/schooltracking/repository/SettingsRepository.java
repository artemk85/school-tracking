package com.artemk.schooltracking.repository;

import com.artemk.schooltracking.domain.Settings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SettingsRepository extends JpaRepository<Settings, Long> {
    Optional<Settings> findByOwnerId(Long ownerId);
}