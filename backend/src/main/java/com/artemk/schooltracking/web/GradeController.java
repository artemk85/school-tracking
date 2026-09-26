package com.artemk.schooltracking.web;

import com.artemk.schooltracking.dto.GradeDto;
import com.artemk.schooltracking.dto.GradeRequest;
import com.artemk.schooltracking.service.GradeService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/grades")
public class GradeController {

    private final GradeService gradeService;

    public GradeController(GradeService gradeService) {
        this.gradeService = gradeService;
    }

    @GetMapping
    public List<GradeDto> all() {
        log.debug("GET /api/grades — fetching all grades");
        return gradeService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GradeDto create(@Valid @RequestBody GradeRequest request) {
        log.debug("POST /api/grades — subjectId={}, value={}, date={}",
                request.subjectId(), request.value(), request.gradeDate());
        return gradeService.create(request);
    }

    @PutMapping("/{id}")
    public GradeDto update(@PathVariable Long id, @Valid @RequestBody GradeRequest request) {
        log.debug("PUT /api/grades/{} — subjectId={}, value={}, date={}",
                id, request.subjectId(), request.value(), request.gradeDate());
        return gradeService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        log.debug("DELETE /api/grades/{}", id);
        gradeService.delete(id);
    }
}