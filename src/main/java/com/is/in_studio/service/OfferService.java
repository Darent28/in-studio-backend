package com.is.in_studio.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.is.in_studio.domain.dto.OfferResponseDto;
import com.is.in_studio.domain.input.OfferInput;
import com.is.in_studio.entity.Offer;
import com.is.in_studio.entity.Plan;
import com.is.in_studio.exception.CustomExceptions.NotFoundException;
import com.is.in_studio.repository.OfferRepository;
import com.is.in_studio.repository.PlanRepository;

import jakarta.transaction.Transactional;

@Service
public class OfferService {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private final OfferRepository offerRepository;
    private final PlanRepository planRepository;

    public OfferService(OfferRepository offerRepository, PlanRepository planRepository) {
        this.offerRepository = offerRepository;
        this.planRepository = planRepository;
    }

    public List<OfferResponseDto> getAll() {
        return offerRepository.findAll().stream()
            .map(OfferResponseDto::fromEntity)
            .toList();
    }

    public List<OfferResponseDto> getByPlan(Integer planId) {
        return offerRepository.findByPlan_PlanId(planId).stream()
            .map(OfferResponseDto::fromEntity)
            .toList();
    }

    @Transactional
    public OfferResponseDto create(OfferInput input) {
        Plan plan = planRepository.findById(input.getPlanId())
            .orElseThrow(() -> new NotFoundException("Plan not found: " + input.getPlanId()));
        Offer offer = new Offer();
        applyInput(offer, input, plan);
        return OfferResponseDto.fromEntity(offerRepository.save(offer));
    }

    @Transactional
    public OfferResponseDto update(Integer id, OfferInput input) {
        Offer offer = offerRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Offer not found: " + id));
        Plan plan = planRepository.findById(input.getPlanId())
            .orElseThrow(() -> new NotFoundException("Plan not found: " + input.getPlanId()));
        applyInput(offer, input, plan);
        return OfferResponseDto.fromEntity(offerRepository.save(offer));
    }

    @Transactional
    public void delete(Integer id) {
        offerRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Offer not found: " + id));
        offerRepository.deleteById(id);
    }

    /**
     * Returns the best (highest discount) active offer for a plan at a given date and time.
     * Checks: date range, day-of-week bitmask, and time window.
     */
    public Optional<OfferResponseDto> validate(Integer planId, LocalDate date, LocalTime time) {
        // bit 0 = Mon (DayOfWeek.MONDAY.getValue() = 1, so bit = 1 << 0 = 1)
        int dayBit = 1 << (date.getDayOfWeek().getValue() - 1);
        return offerRepository.findCandidateOffers(planId, date, time)
            .stream()
            .filter(o -> {
                Integer mask = o.getDaysOfWeek();
                return mask == null || mask == 0 || (mask & dayBit) != 0;
            })
            .findFirst()
            .map(OfferResponseDto::fromEntity);
    }

    private void applyInput(Offer offer, OfferInput input, Plan plan) {
        offer.setPlan(plan);
        offer.setDiscountPercent(input.getDiscountPercent());
        offer.setDaysOfWeek(input.getDaysOfWeek());
        offer.setStartDate(input.getStartDate() != null && !input.getStartDate().isBlank()
            ? LocalDate.parse(input.getStartDate()) : null);
        offer.setEndDate(input.getEndDate() != null && !input.getEndDate().isBlank()
            ? LocalDate.parse(input.getEndDate()) : null);
        offer.setStartHour(input.getStartHour() != null && !input.getStartHour().isBlank()
            ? LocalTime.parse(input.getStartHour(), TIME_FMT) : null);
        offer.setEndHour(input.getEndHour() != null && !input.getEndHour().isBlank()
            ? LocalTime.parse(input.getEndHour(), TIME_FMT) : null);
        offer.setActive(input.getActive() != null ? input.getActive() : true);
    }
}
