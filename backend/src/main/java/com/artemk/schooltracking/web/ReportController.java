package com.artemk.schooltracking.web;

import com.artemk.schooltracking.domain.Settings;
import com.artemk.schooltracking.dto.AllTimeStats;
import com.artemk.schooltracking.dto.WeeklyReport;
import com.artemk.schooltracking.service.RewardService;
import com.artemk.schooltracking.service.SettingsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/api")
public class ReportController {

    private final RewardService rewardService;
    private final SettingsService settingsService;

    public ReportController(RewardService rewardService, SettingsService settingsService) {
        this.rewardService = rewardService;
        this.settingsService = settingsService;
    }

    @GetMapping("/report/week")
    public WeeklyReport week(@RequestParam(required = false) String date) {
        LocalDate reference = (date == null || date.isBlank()) ? LocalDate.now() : LocalDate.parse(date);
        log.debug("GET /api/report/week?date={}", reference);
        return rewardService.buildWeeklyReport(reference, settingsService.get());
    }

    @GetMapping("/stats/all")
    public AllTimeStats allStats() {
        log.debug("GET /api/stats/all");
        return rewardService.buildAllTimeStats(settingsService.get());
    }

    @GetMapping("/settings")
    public Settings settings() {
        log.debug("GET /api/settings");
        return settingsService.get();
    }

    @PutMapping("/settings")
    public Settings updateSettings(@RequestBody Settings settings) {
        log.debug("PUT /api/settings — {}", settings);
        return settingsService.update(settings);
    }
}