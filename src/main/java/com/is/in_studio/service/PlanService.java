package com.is.in_studio.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.is.in_studio.domain.dto.PlanResponseDto;
import com.is.in_studio.domain.input.PlanInput;
import com.is.in_studio.entity.Offer;
import com.is.in_studio.entity.Plan;
import com.is.in_studio.exception.CustomExceptions.NotFoundException;
import com.is.in_studio.exception.CustomExceptions.ServerErrorException;
import com.is.in_studio.repository.OfferRepository;
import com.is.in_studio.repository.PlanRepository;

import jakarta.transaction.Transactional;

@Service
public class PlanService {

    private static final Logger log = LoggerFactory.getLogger(PlanService.class);

    private final PlanRepository planRepository;
    private final OfferRepository offerRepository;

    public PlanService(PlanRepository planRepository, OfferRepository offerRepository) {
        this.planRepository = planRepository;
        this.offerRepository = offerRepository;
    }

    public List<PlanResponseDto> getAll() {
        try {
            return planRepository.findAll().stream()
                .map(plan -> PlanResponseDto.fromEntity(plan, bestDiscount(plan.getPlanId())))
                .toList();
        } catch (Exception e) {
            log.error("Failed to retrieve plans", e);
            throw new ServerErrorException("Failed to retrieve plans.");
        }
    }

    public PlanResponseDto getById(Integer id) {
        Plan plan = planRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Plan not found with id: " + id));
        return PlanResponseDto.fromEntity(plan, bestDiscount(plan.getPlanId()));
    }

    @Transactional
    public PlanResponseDto create(PlanInput input) {
        try {
            Plan plan = new Plan();
            applyInput(plan, input);
            planRepository.save(plan);
            return PlanResponseDto.fromEntity(plan);
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to create plan", e);
            throw new ServerErrorException("Failed to create plan.");
        }
    }

    @Transactional
    public PlanResponseDto update(Integer id, PlanInput input) {
        Plan plan = planRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Plan not found with id: " + id));
        try {
            applyInput(plan, input);
            planRepository.save(plan);
            return PlanResponseDto.fromEntity(plan);
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to update plan id={}", id, e);
            throw new ServerErrorException("Failed to update plan.");
        }
    }

    @Transactional
    public void delete(Integer id) {
        planRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Plan not found with id: " + id));
        planRepository.deleteById(id);
    }

    private Integer bestDiscount(Integer planId) {
        return offerRepository.findActiveByPlanId(planId)
            .stream()
            .map(Offer::getDiscountPercent)
            .findFirst()
            .orElse(null);
    }

    private void applyInput(Plan plan, PlanInput input) {
        plan.setName(input.getName());
        plan.setCredits(input.getCredits());
        plan.setPrice(input.getPrice());
        plan.setDurationDays(input.getDurationDays());
        plan.setType(input.getType());
        plan.setActive(input.getActive() != null ? input.getActive() : true);
    }
}
