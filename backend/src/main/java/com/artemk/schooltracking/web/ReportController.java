package com.artemk.schooltracking.web;

import com.artemk.schooltracking.dto.AllTimeStats;
import com.artemk.schooltracking.dto.SettingsDto;
import com.artemk.schooltracking.dto.WeeklyReport;
import com.artemk.schooltracking.service.CurrentUser;
import com.artemk.schooltracking.service.RewardService;
import com.artemk.schooltracking.service.SettingsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/api")
public class ReportController {

    private final RewardService rewardService;
    private final SettingsService settingsService;
    private final CurrentUser currentUser;

    public ReportController(RewardService rewardService,
                            SettingsService settingsService,
                            CurrentUser currentUser) {
        this.rewardService = rewardService;
        this.settingsService = settingsService;
        this.currentUser = currentUser;
    }

    @GetMapping("/report/week")
    public WeeklyReport week(@RequestParam(required = false) String date,
                             @RequestParam(required = false) Long childId) {
        LocalDate reference = (date == null || date.isBlank()) ? LocalDate.now() : LocalDate.parse(date);
        Long ownerId = currentUser.ownerId();
        Long effectiveChild = currentUser.effectiveChildId(childId);
        log.debug("GET /api/report/week?date={}&childId={}", reference, effectiveChild);
        return rewardService.buildWeeklyReport(ownerId, effectiveChild, reference, settingsService.get(ownerId));
    }

    @GetMapping("/stats/all")
    public AllTimeStats allStats(@RequestParam(required = false) Long childId) {
        Long ownerId = currentUser.ownerId();
        Long effectiveChild = currentUser.effectiveChildId(childId);
        log.debug("GET /api/stats/all?childId={}", effectiveChild);
        return rewardService.buildAllTimeStats(ownerId, effectiveChild, settingsService.get(ownerId));
    }

    @GetMapping("/settings")
    public SettingsDto settings() {
        log.debug("GET /api/settings");
        return SettingsDto.from(settingsService.get(currentUser.ownerId()));
    }

    @PutMapping("/settings")
    @PreAuthorize("hasRole('PARENT')")
    public SettingsDto updateSettings(@RequestBody SettingsDto settings) {
        log.debug("PUT /api/settings");
        return settingsService.update(currentUser.ownerId(), settings);
    }
}