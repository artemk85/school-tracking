package com.artemk.schooltracking.service;

import com.artemk.schooltracking.domain.Subject;
import com.artemk.schooltracking.domain.User;
import com.artemk.schooltracking.dto.SubjectDto;
import com.artemk.schooltracking.repository.GradeRepository;
import com.artemk.schooltracking.repository.SubjectRepository;
import com.artemk.schooltracking.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;
    private final GradeRepository gradeRepository;
    private final CurrentUser currentUser;

    public SubjectService(SubjectRepository subjectRepository,
                          UserRepository userRepository,
                          GradeRepository gradeRepository,
                          CurrentUser currentUser) {
        this.subjectRepository = subjectRepository;
        this.userRepository = userRepository;
        this.gradeRepository = gradeRepository;
        this.currentUser = currentUser;
    }

    public List<SubjectDto> findAll() {
        Long ownerId = currentUser.ownerId();
        return subjectRepository.findByOwnerIdOrderByCoreDescNameAsc(ownerId).stream()
                .map(s -> new SubjectDto(s.getId(), s.getName(), s.isCore()))
                .toList();
    }

    public SubjectDto create(SubjectDto request) {
        Long ownerId = currentUser.ownerId();
        if (subjectRepository.existsByOwnerIdAndNameIgnoreCase(ownerId, request.name())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Предмет уже существует");
        }
        User owner = userRepository.getReferenceById(ownerId);
        Subject saved = subjectRepository.save(new Subject(owner, request.name(), request.core()));
        return new SubjectDto(saved.getId(), saved.getName(), saved.isCore());
    }

    public SubjectDto update(Long id, SubjectDto request) {
        Long ownerId = currentUser.ownerId();
        Subject subject = subjectRepository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Предмет не найден"));
        subjectRepository.findByOwnerIdAndNameIgnoreCase(ownerId, request.name())
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Предмет уже существует");
                });
        subject.setName(request.name());
        subject.setCore(request.core());
        Subject saved = subjectRepository.save(subject);
        return new SubjectDto(saved.getId(), saved.getName(), saved.isCore());
    }

    public void delete(Long id) {
        Long ownerId = currentUser.ownerId();
        Subject subject = subjectRepository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Предмет не найден"));
        long grades = gradeRepository.countBySubjectId(id);
        if (grades > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Нельзя удалить предмет: есть связанные оценки (" + grades + ")");
        }
        subjectRepository.delete(subject);
    }
}