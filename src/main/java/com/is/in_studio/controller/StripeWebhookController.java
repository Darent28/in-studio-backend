package com.is.in_studio.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.is.in_studio.domain.input.PaymentInput;
import com.is.in_studio.repository.PlanRepository;
import com.is.in_studio.service.PaymentService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class StripeWebhookController {

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    private final PaymentService paymentService;
    private final PlanRepository planRepository;

    public StripeWebhookController(PaymentService paymentService,
                                   PlanRepository planRepository) {
        this.paymentService = paymentService;
        this.planRepository = planRepository;
    }

    @PostMapping("/api/stripe/webhook")
    public ResponseEntity<String> handleWebhook(HttpServletRequest request) {
        String sigHeader = request.getHeader("Stripe-Signature");

        String payload;
        try {
            payload = new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Failed to read request body");
        }

        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            return ResponseEntity.badRequest().body("Invalid Stripe signature");
        }

        if ("payment_intent.succeeded".equals(event.getType())) {
            event.getDataObjectDeserializer().getObject().ifPresent(obj -> {
                if (obj instanceof PaymentIntent intent) {
                    Long userId = Long.valueOf(intent.getMetadata().get("userId"));
                    Integer planId = Integer.valueOf(intent.getMetadata().get("planId"));

                    var plan = planRepository.findById(planId).orElse(null);
                    if (plan == null) return;

                    PaymentInput paymentInput = new PaymentInput();
                    paymentInput.setUserId(userId);
                    paymentInput.setPlanId(planId);
                    paymentInput.setAmount(plan.getPrice());
                    paymentInput.setCurrency("MXN");
                    paymentInput.setMethod(com.is.in_studio.entity.Payment.PaymentMethod.STRIPE);
                    paymentInput.setTransactionRef(intent.getId());
                    paymentService.create(paymentInput);
                }
            });
        }

        return ResponseEntity.ok("received");
    }
}
