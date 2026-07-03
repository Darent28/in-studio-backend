package com.is.in_studio.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.is.in_studio.auth.JwtUtil;
import com.is.in_studio.domain.dto.CouponResponseDto;
import com.is.in_studio.domain.input.CouponInput;
import com.is.in_studio.service.CouponService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
public class CouponController {

    private final CouponService couponService;
    private final JwtUtil jwtUtil;

    public CouponController(CouponService couponService, JwtUtil jwtUtil) {
        this.couponService = couponService;
        this.jwtUtil = jwtUtil;
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

    @GetMapping("/api/coupons/validate")
    public ResponseEntity<CouponResponseDto> validate(@RequestParam String code,
                                                      @RequestParam Integer planId,
                                                      HttpServletRequest request) {
        Long userId = extractUserId(request);
        return couponService.validate(code, planId, userId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.noContent().build());
    }

    private Long extractUserId(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        return jwtUtil.extractUserId(header.substring(7));
    }
}
