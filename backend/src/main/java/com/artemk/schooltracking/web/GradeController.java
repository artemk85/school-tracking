package com.artemk.schooltracking.web;

import com.artemk.schooltracking.dto.GradeDto;
import com.artemk.schooltracking.dto.GradeRequest;
import com.artemk.schooltracking.service.GradeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/grades")
public class GradeController {

    private final GradeService gradeService;

    public GradeController(GradeService gradeService) {
        this.gradeService = gradeService;
    }

    @GetMapping
    public List<GradeDto> all() {
        return gradeService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GradeDto create(@Valid @RequestBody GradeRequest request) {
        return gradeService.create(request);
    }

    @PutMapping("/{id}")
    public GradeDto update(@PathVariable Long id, @Valid @RequestBody GradeRequest request) {
        return gradeService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        gradeService.delete(id);
    }
}