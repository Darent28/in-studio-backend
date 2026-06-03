package com.is.in_studio.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.is.in_studio.domain.dto.PlanResponseDto;
import com.is.in_studio.domain.input.PlanInput;
import com.is.in_studio.service.PlanService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/plans")
public class PlanController {

    private final PlanService planService;

    public PlanController(PlanService planService) {
        this.planService = planService;
    }

    @GetMapping
    public List<PlanResponseDto> getAll() {
        return planService.getAll();
    }

    @GetMapping("/{id}")
    public PlanResponseDto getById(@PathVariable Integer id) {
        return planService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlanResponseDto create(@Valid @RequestBody PlanInput input) {
        return planService.create(input);
    }

    @PutMapping("/{id}")
    public PlanResponseDto update(@PathVariable Integer id, @Valid @RequestBody PlanInput input) {
        return planService.update(id, input);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        planService.delete(id);
    }
}
