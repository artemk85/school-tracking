package com.artemk.schooltracking.service;

import com.artemk.schooltracking.domain.Grade;
import com.artemk.schooltracking.domain.Settings;
import com.artemk.schooltracking.domain.Subject;
import com.artemk.schooltracking.dto.GradeDto;
import com.artemk.schooltracking.dto.GradeRequest;
import com.artemk.schooltracking.repository.GradeRepository;
import com.artemk.schooltracking.repository.SubjectRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@Service
public class GradeService {

    private final GradeRepository gradeRepository;
    private final SubjectRepository subjectRepository;
    private final SettingsService settingsService;
    private final RewardService rewardService;

    public GradeService(GradeRepository gradeRepository,
                        SubjectRepository subjectRepository,
                        SettingsService settingsService,
                        RewardService rewardService) {
        this.gradeRepository = gradeRepository;
        this.subjectRepository = subjectRepository;
        this.settingsService = settingsService;
        this.rewardService = rewardService;
    }

    public List<GradeDto> findAll() {
        log.debug("Fetching all grades");
        Settings settings = settingsService.get();
        return gradeRepository.findAll().stream()
                .sorted((a, b) -> b.getGradeDate().compareTo(a.getGradeDate()))
                .map(g -> rewardService.toDto(g, settings))
                .toList();
    }

    public GradeDto create(GradeRequest request) {
        log.debug("Creating grade: subjectId={}, value={}, date={}",
                request.subjectId(), request.value(), request.gradeDate());
        Subject subject = subjectRepository.findById(request.subjectId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Предмет не найден"));
        Grade grade = new Grade(subject, request.value(), request.gradeDate());
        Grade saved = gradeRepository.save(grade);
        return rewardService.toDto(saved, settingsService.get());
    }

    public GradeDto update(Long id, GradeRequest request) {
        log.debug("Updating grade {}: subjectId={}, value={}, date={}",
                id, request.subjectId(), request.value(), request.gradeDate());
        Grade grade = gradeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Оценка не найдена"));
        Subject subject = subjectRepository.findById(request.subjectId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Предмет не найден"));
        grade.setSubject(subject);
        grade.setValue(request.value());
        grade.setGradeDate(request.gradeDate());
        return rewardService.toDto(gradeRepository.save(grade), settingsService.get());
    }

    public void delete(Long id) {
        log.debug("Deleting grade {}", id);
        if (!gradeRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Оценка не найдена");
        }
        gradeRepository.deleteById(id);
    }
}