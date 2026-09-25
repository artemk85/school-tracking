package com.artemk.schooltracking.service;

import com.artemk.schooltracking.domain.Grade;
import com.artemk.schooltracking.domain.Settings;
import com.artemk.schooltracking.domain.Subject;
import com.artemk.schooltracking.dto.GradeDto;
import com.artemk.schooltracking.dto.SubjectWeeklyResult;
import com.artemk.schooltracking.dto.WeeklyReport;
import com.artemk.schooltracking.repository.GradeRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

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
        return new GradeDto(
                grade.getId(),
                subject.getId(),
                subject.getName(),
                subject.isCore(),
                grade.getValue(),
                grade.getGradeDate(),
                coefficientFor(subject, settings).setScale(4, RoundingMode.HALF_UP),
                amountFor(grade.getValue(), subject, settings)
        );
    }

    public WeeklyReport buildWeeklyReport(LocalDate anyDateInWeek, Settings settings) {
        LocalDate start = weekStart(anyDateInWeek);
        LocalDate end = start.plusDays(6);

        List<Grade> grades = gradeRepository.findByGradeDateBetween(start, end);

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

        return new WeeklyReport(start, end, total, subjects);
    }
}