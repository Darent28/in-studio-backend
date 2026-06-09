package com.is.in_studio.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.is.in_studio.domain.dto.PaymentResponseDto;
import com.is.in_studio.domain.input.PaymentInput;
import com.is.in_studio.service.PaymentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/payments")
public class AdminPaymentController {

    private final PaymentService paymentService;

    public AdminPaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping
    public List<PaymentResponseDto> getAll() {
        return paymentService.getAll();
    }

    @GetMapping("/membership/{membershipId}")
    public List<PaymentResponseDto> getByMembership(@PathVariable Long membershipId) {
        return paymentService.getByMembership(membershipId);
    }

    @GetMapping("/{id}")
    public PaymentResponseDto getById(@PathVariable Long id) {
        return paymentService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponseDto create(@Valid @RequestBody PaymentInput input) {
        return paymentService.create(input);
    }

    @PatchMapping("/{id}/confirm")
    public PaymentResponseDto confirm(@PathVariable Long id) {
        return paymentService.confirm(id);
    }
}
