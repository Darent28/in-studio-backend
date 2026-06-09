package com.is.in_studio.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.is.in_studio.domain.dto.MembershipResponseDto;
import com.is.in_studio.domain.input.AdjustCreditsInput;
import com.is.in_studio.domain.input.ChangePeriodInput;
import com.is.in_studio.domain.input.MembershipInput;
import com.is.in_studio.entity.Membership.MembershipStatus;
import com.is.in_studio.service.MembershipService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/memberships")
public class MembershipController {

    private final MembershipService membershipService;

    public MembershipController(MembershipService membershipService) {
        this.membershipService = membershipService;
    }

    @GetMapping
    public List<MembershipResponseDto> getAll() {
        return membershipService.getAll();
    }

    @GetMapping("/user/{userId}")
    public List<MembershipResponseDto> getByUser(@PathVariable Long userId) {
        return membershipService.getByUser(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MembershipResponseDto create(@RequestBody @Valid MembershipInput input) {
        return membershipService.create(input);
    }

    @PatchMapping("/{id}/credits")
    public MembershipResponseDto adjustCredits(@PathVariable Long id,
                                               @RequestBody @Valid AdjustCreditsInput input) {
        return membershipService.adjustCredits(id, input);
    }

    @PatchMapping("/{id}/status")
    public MembershipResponseDto changeStatus(@PathVariable Long id,
                                              @RequestParam MembershipStatus status) {
        return membershipService.changeStatus(id, status);
    }

    @PatchMapping("/{id}/period")
    public MembershipResponseDto changePeriod(@PathVariable Long id,
                                              @RequestBody @Valid ChangePeriodInput input) {
        return membershipService.changePeriod(id, input);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        membershipService.delete(id);
    }
}
