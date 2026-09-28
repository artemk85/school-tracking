package com.artemk.schooltracking.service;

import com.artemk.schooltracking.domain.Grade;
import com.artemk.schooltracking.domain.Settings;
import com.artemk.schooltracking.domain.Subject;
import com.artemk.schooltracking.domain.User;
import com.artemk.schooltracking.dto.GradeDto;
import com.artemk.schooltracking.dto.GradeRequest;
import com.artemk.schooltracking.repository.GradeRepository;
import com.artemk.schooltracking.repository.SubjectRepository;
import com.artemk.schooltracking.repository.UserRepository;
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
    private final UserRepository userRepository;
    private final SettingsService settingsService;
    private final RewardService rewardService;
    private final CurrentUser currentUser;

    public GradeService(GradeRepository gradeRepository,
                        SubjectRepository subjectRepository,
                        UserRepository userRepository,
                        SettingsService settingsService,
                        RewardService rewardService,
                        CurrentUser currentUser) {
        this.gradeRepository = gradeRepository;
        this.subjectRepository = subjectRepository;
        this.userRepository = userRepository;
        this.settingsService = settingsService;
        this.rewardService = rewardService;
        this.currentUser = currentUser;
    }

    public List<GradeDto> findAll(Long requestedChildId) {
        Long ownerId = currentUser.ownerId();
        Long childId = currentUser.effectiveChildId(requestedChildId);
        ensureChildBelongsToOwner(ownerId, childId);
        log.debug("Fetching grades for owner={}, child={}", ownerId, childId);
        Settings settings = settingsService.get(ownerId);
        return gradeRepository.findByOwnerIdAndChildId(ownerId, childId).stream()
                .sorted((a, b) -> b.getGradeDate().compareTo(a.getGradeDate()))
                .map(g -> rewardService.toDto(g, settings))
                .toList();
    }

    public GradeDto create(GradeRequest request) {
        Long ownerId = currentUser.ownerId();
        Long childId = currentUser.effectiveChildId(request.childId());
        User child = ensureChildBelongsToOwner(ownerId, childId);
        Subject subject = subjectRepository.findByIdAndOwnerId(request.subjectId(), ownerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Предмет не найден"));
        log.debug("Creating grade: owner={}, child={}, subjectId={}, value={}, date={}",
                ownerId, childId, request.subjectId(), request.value(), request.gradeDate());
        User owner = child.getParent() != null ? child.getParent() : child;
        Grade grade = new Grade(owner, child, subject, request.value(), request.gradeDate());
        Grade saved = gradeRepository.save(grade);
        return rewardService.toDto(saved, settingsService.get(ownerId));
    }

    public GradeDto update(Long id, GradeRequest request) {
        Long ownerId = currentUser.ownerId();
        Grade grade = gradeRepository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Оценка не найдена"));
        Long childId = currentUser.effectiveChildId(request.childId() != null ? request.childId() : grade.getChild().getId());
        ensureChildBelongsToOwner(ownerId, childId);
        Subject subject = subjectRepository.findByIdAndOwnerId(request.subjectId(), ownerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Предмет не найден"));
        log.debug("Updating grade {}: owner={}, child={}", id, ownerId, childId);
        grade.setSubject(subject);
        grade.setValue(request.value());
        grade.setGradeDate(request.gradeDate());
        return rewardService.toDto(gradeRepository.save(grade), settingsService.get(ownerId));
    }

    public void delete(Long id) {
        Long ownerId = currentUser.ownerId();
        log.debug("Deleting grade {} for owner={}", id, ownerId);
        Grade grade = gradeRepository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Оценка не найдена"));
        gradeRepository.delete(grade);
    }

    private User ensureChildBelongsToOwner(Long ownerId, Long childId) {
        return userRepository.findByIdAndParent_Id(childId, ownerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Ребёнок недоступен"));
    }
}