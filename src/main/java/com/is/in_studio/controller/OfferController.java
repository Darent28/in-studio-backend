package com.is.in_studio.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.is.in_studio.domain.dto.OfferResponseDto;
import com.is.in_studio.domain.input.OfferInput;
import com.is.in_studio.service.OfferService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/offers")
public class OfferController {

    private final OfferService offerService;

    public OfferController(OfferService offerService) {
        this.offerService = offerService;
    }

    @GetMapping
    public List<OfferResponseDto> getAll(@RequestParam(required = false) Integer planId) {
        return planId != null ? offerService.getByPlan(planId) : offerService.getAll();
    }

    /**
     * Validates whether an active offer exists for a plan at a specific date and time.
     * Returns 200 with the best matching offer, or 204 if none found.
     */
    @GetMapping("/validate")
    public ResponseEntity<OfferResponseDto> validate(
        @RequestParam Integer planId,
        @RequestParam String date,
        @RequestParam String time
    ) {
        return offerService.validate(planId, LocalDate.parse(date), LocalTime.parse(time))
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.noContent().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OfferResponseDto create(@RequestBody @Valid OfferInput input) {
        return offerService.create(input);
    }

    @PutMapping("/{id}")
    public OfferResponseDto update(@PathVariable Integer id, @RequestBody @Valid OfferInput input) {
        return offerService.update(id, input);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        offerService.delete(id);
    }
}
