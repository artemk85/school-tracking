package com.artemk.schooltracking.web;

import com.artemk.schooltracking.dto.SubjectDto;
import com.artemk.schooltracking.service.SubjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subjects")
public class SubjectController {

    private final SubjectService subjectService;

    public SubjectController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @GetMapping
    public List<SubjectDto> all() {
        return subjectService.findAll();
    }

    @PostMapping
    @PreAuthorize("hasRole('PARENT')")
    @ResponseStatus(HttpStatus.CREATED)
    public SubjectDto create(@Valid @RequestBody SubjectDto request) {
        return subjectService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PARENT')")
    public SubjectDto update(@PathVariable Long id, @Valid @RequestBody SubjectDto request) {
        return subjectService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PARENT')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        subjectService.delete(id);
    }
}