package com.is.in_studio.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.is.in_studio.domain.input.PaymentInput;
import com.is.in_studio.entity.User;
import com.is.in_studio.entity.UserPaymentMethod;
import com.is.in_studio.exception.CustomExceptions.NotFoundException;
import com.is.in_studio.repository.UserPaymentMethodRepository;
import com.is.in_studio.repository.UserRepository;
import com.is.in_studio.repository.PlanRepository;
import com.is.in_studio.service.PaymentService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import com.stripe.net.Webhook;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class StripeWebhookController {

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    private final PaymentService paymentService;
    private final PlanRepository planRepository;
    private final UserRepository userRepository;
    private final UserPaymentMethodRepository paymentMethodRepository;

    public StripeWebhookController(PaymentService paymentService,
                                   PlanRepository planRepository,
                                   UserRepository userRepository,
                                   UserPaymentMethodRepository paymentMethodRepository) {
        this.paymentService = paymentService;
        this.planRepository = planRepository;
        this.userRepository = userRepository;
        this.paymentMethodRepository = paymentMethodRepository;
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
                    var payment = paymentService.create(paymentInput);
                    paymentService.confirm(payment.paymentId());

                    savePaymentMethodIfCard(userId, intent);
                }
            });
        }

        return ResponseEntity.ok("received");
    }

    private void savePaymentMethodIfCard(Long userId, PaymentIntent intent) {
        String pmId = intent.getPaymentMethod();
        if (pmId == null) return;

        if (paymentMethodRepository.existsByUser_UserIdAndStripePaymentMethodId(userId, pmId)) return;

        try {
            PaymentMethod pm = PaymentMethod.retrieve(pmId);
            if (!"card".equals(pm.getType())) return;

            User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));

            boolean isFirst = paymentMethodRepository
                .findByUser_UserIdOrderByIsDefaultDescCreatedAtDesc(userId).isEmpty();

            UserPaymentMethod saved = new UserPaymentMethod();
            saved.setUser(user);
            saved.setStripePaymentMethodId(pmId);
            saved.setBrand(pm.getCard().getBrand());
            saved.setLast4(pm.getCard().getLast4());
            saved.setExpMonth(pm.getCard().getExpMonth().intValue());
            saved.setExpYear(pm.getCard().getExpYear().intValue());
            saved.setIsDefault(isFirst);
            paymentMethodRepository.save(saved);
        } catch (StripeException e) {
            // log but don't fail — membership is already created
        }
    }
}
