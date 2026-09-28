package com.artemk.schooltracking.service;

import com.artemk.schooltracking.domain.Grade;
import com.artemk.schooltracking.domain.Settings;
import com.artemk.schooltracking.domain.Subject;
import com.artemk.schooltracking.domain.User;
import com.artemk.schooltracking.dto.GradeDto;
import com.artemk.schooltracking.dto.SubjectWeeklyResult;
import com.artemk.schooltracking.dto.WeeklyReport;
import com.artemk.schooltracking.repository.GradeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.artemk.schooltracking.dto.AllTimeStats;
import com.artemk.schooltracking.dto.WeeklyStats;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RewardService {

    private static final int SCALE = 2;

    private final GradeRepository gradeRepository;

    public RewardService(GradeRepository gradeRepository) {
        this.gradeRepository = gradeRepository;
    }

    public LocalDate weekStart(LocalDate date) {
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    public LocalDate weekEnd(LocalDate date) {
        return weekStart(date).plusDays(6);
    }

    /**
     * Базовая стоимость оценки (коэффициент 1.0).
     * 5 = +75, 4 = +50, 3 = -50, 2 = -100.
     * Двойка (-100) плюс две пятёрки (+75 +75) дают ровно +50, то есть
     * фактически исправление двойки на четвёрку двумя пятёрками.
     */
    public BigDecimal baseValue(int grade, Settings settings) {
        return switch (grade) {
            case 5 -> settings.getFiveReward();
            case 4 -> settings.getFourReward();
            case 3 -> settings.getThreePenalty();
            case 2 -> settings.getTwoPenalty();
            default -> throw new IllegalArgumentException("Недопустимая оценка: " + grade);
        };
    }

    public BigDecimal coefficientFor(Subject subject, Settings settings) {
        return subject.isCore() ? settings.getCoreCoefficient() : settings.getOtherCoefficient();
    }

    public BigDecimal amountFor(int grade, Subject subject, Settings settings) {
        return baseValue(grade, settings)
                .multiply(coefficientFor(subject, settings))
                .setScale(SCALE, RoundingMode.HALF_UP);
    }

    public GradeDto toDto(Grade grade, Settings settings) {
        Subject subject = grade.getSubject();
        User child = grade.getChild();
        return new GradeDto(
                grade.getId(),
                child == null ? null : child.getId(),
                child == null ? null : child.getDisplayName(),
                subject.getId(),
                subject.getName(),
                subject.isCore(),
                grade.getValue(),
                grade.getGradeDate(),
                coefficientFor(subject, settings).setScale(4, RoundingMode.HALF_UP),
                amountFor(grade.getValue(), subject, settings)
        );
    }

    public WeeklyReport buildWeeklyReport(Long ownerId, Long childId, LocalDate anyDateInWeek, Settings settings) {
        LocalDate start = weekStart(anyDateInWeek);
        LocalDate end = start.plusDays(6);

        log.debug("Building weekly report: owner={}, child={}, {} — {}, settings={}", ownerId, childId, start, end, settings);

        List<Grade> grades = gradeRepository.findByOwnerIdAndChildIdAndGradeDateBetween(ownerId, childId, start, end);
        log.debug("Found {} grades in date range", grades.size());

        Map<Long, List<Grade>> grouped = grades.stream()
                .collect(Collectors.groupingBy(g -> g.getSubject().getId(), LinkedHashMap::new, Collectors.toList()));

        List<SubjectWeeklyResult> subjects = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP);

        for (List<Grade> subjectGrades : grouped.values()) {
            Subject subject = subjectGrades.get(0).getSubject();
            List<GradeDto> dtos = subjectGrades.stream()
                    .sorted(Comparator.comparing(Grade::getGradeDate).thenComparing(Grade::getId))
                    .map(g -> toDto(g, settings))
                    .toList();

            BigDecimal amount = dtos.stream()
                    .map(GradeDto::amount)
                    .reduce(BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP), BigDecimal::add);

            subjects.add(new SubjectWeeklyResult(
                    subject.getId(),
                    subject.getName(),
                    subject.isCore(),
                    coefficientFor(subject, settings).setScale(4, RoundingMode.HALF_UP),
                    dtos,
                    amount
            ));

            total = total.add(amount);
        }

        subjects.sort(Comparator
                .comparing(SubjectWeeklyResult::core).reversed()
                .thenComparing(SubjectWeeklyResult::subjectName));

        log.debug("Weekly report built: {} subjects, total={}", subjects.size(), total);

        return new WeeklyReport(start, end, total, subjects);
    }

    public AllTimeStats buildAllTimeStats(Long ownerId, Long childId, Settings settings) {
        List<Grade> allGrades = gradeRepository.findByOwnerIdAndChildId(ownerId, childId);

        if (allGrades.isEmpty()) {
            return new AllTimeStats(List.of(), BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP), 0);
        }

        Map<LocalDate, List<Grade>> gradesByWeek = allGrades.stream()
                .collect(Collectors.groupingBy(
                        g -> weekStart(g.getGradeDate()),
                        TreeMap::new,
                        Collectors.toList()
                ));

        List<WeeklyStats> weeks = new ArrayList<>();
        BigDecimal grandTotal = BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP);
        int totalGrades = 0;

        for (Map.Entry<LocalDate, List<Grade>> entry : gradesByWeek.entrySet()) {
            LocalDate start = entry.getKey();
            LocalDate end = start.plusDays(6);
            List<Grade> weekGrades = entry.getValue();

            BigDecimal weekTotal = weekGrades.stream()
                    .map(g -> amountFor(g.getValue(), g.getSubject(), settings))
                    .reduce(BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP), BigDecimal::add);

            weeks.add(new WeeklyStats(start, end, weekGrades.size(), weekTotal));
            grandTotal = grandTotal.add(weekTotal);
            totalGrades += weekGrades.size();
        }

        log.debug("All-time stats built: {} weeks, {} grades, total={}", weeks.size(), totalGrades, grandTotal);

        return new AllTimeStats(weeks, grandTotal, totalGrades);
    }
}