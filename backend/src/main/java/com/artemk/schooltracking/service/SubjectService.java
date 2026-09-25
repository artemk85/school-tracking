package com.artemk.schooltracking.service;

import com.artemk.schooltracking.domain.Subject;
import com.artemk.schooltracking.dto.SubjectDto;
import com.artemk.schooltracking.repository.SubjectRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;

@Service
public class SubjectService {

    private final SubjectRepository subjectRepository;

    public SubjectService(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }

    public List<SubjectDto> findAll() {
        return subjectRepository.findAll().stream()
                .sorted(Comparator
                        .comparing(Subject::isCore).reversed()
                        .thenComparing(Subject::getName, String.CASE_INSENSITIVE_ORDER))
                .map(s -> new SubjectDto(s.getId(), s.getName(), s.isCore()))
                .toList();
    }

    public SubjectDto create(SubjectDto request) {
        subjectRepository.findByNameIgnoreCase(request.name()).ifPresent(s -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Предмет уже существует");
        });
        Subject saved = subjectRepository.save(new Subject(request.name(), request.core()));
        return new SubjectDto(saved.getId(), saved.getName(), saved.isCore());
    }

    public SubjectDto update(Long id, SubjectDto request) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Предмет не найден"));
        subject.setName(request.name());
        subject.setCore(request.core());
        Subject saved = subjectRepository.save(subject);
        return new SubjectDto(saved.getId(), saved.getName(), saved.isCore());
    }

    public void delete(Long id) {
        if (!subjectRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Предмет не найден");
        }
        subjectRepository.deleteById(id);
    }
}