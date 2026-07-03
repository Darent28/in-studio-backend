package com.is.in_studio.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.is.in_studio.domain.dto.CouponResponseDto;
import com.is.in_studio.domain.input.CouponInput;
import com.is.in_studio.service.CouponService;

import jakarta.validation.Valid;

@RestController
public class CouponController {

    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @GetMapping("/api/admin/coupons")
    public List<CouponResponseDto> getAll() {
        return couponService.getAll();
    }

    @PostMapping("/api/admin/coupons")
    @ResponseStatus(HttpStatus.CREATED)
    public CouponResponseDto create(@RequestBody @Valid CouponInput input) {
        return couponService.create(input);
    }

    @PutMapping("/api/admin/coupons/{id}")
    public CouponResponseDto update(@PathVariable Integer id, @RequestBody @Valid CouponInput input) {
        return couponService.update(id, input);
    }

    @DeleteMapping("/api/admin/coupons/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        couponService.delete(id);
    }

    /** Validates whether a coupon code is applicable for a given plan. Requires auth. */
    @GetMapping("/api/coupons/validate")
    public ResponseEntity<CouponResponseDto> validate(@RequestParam String code, @RequestParam Integer planId) {
        return couponService.validate(code, planId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.noContent().build());
    }
}
