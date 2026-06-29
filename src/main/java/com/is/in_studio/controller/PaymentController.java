package com.is.in_studio.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.is.in_studio.auth.JwtUtil;
import com.is.in_studio.domain.dto.PaymentResponseDto;
import com.is.in_studio.domain.input.PaymentInput;
import com.is.in_studio.entity.Plan;
import com.is.in_studio.entity.User;
import com.is.in_studio.exception.CustomExceptions.NotFoundException;
import com.is.in_studio.repository.OfferRepository;
import com.is.in_studio.repository.PlanRepository;
import com.is.in_studio.repository.UserRepository;
import com.is.in_studio.service.EmailService;
import com.is.in_studio.service.PaymentService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentIntent;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.PaymentIntentCreateParams;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Value("${stripe.api-key}")
    private String stripeApiKey;

    private final JwtUtil jwtUtil;
    private final PaymentService paymentService;
    private final PlanRepository planRepository;
    private final UserRepository userRepository;
    private final OfferRepository offerRepository;
    private final EmailService emailService;

    public PaymentController(JwtUtil jwtUtil,
                             PaymentService paymentService,
                             PlanRepository planRepository,
                             UserRepository userRepository,
                             OfferRepository offerRepository,
                             EmailService emailService) {
        this.jwtUtil = jwtUtil;
        this.paymentService = paymentService;
        this.planRepository = planRepository;
        this.userRepository = userRepository;
        this.offerRepository = offerRepository;
        this.emailService = emailService;
    }

    @PostConstruct
    public void initStripe() {
        Stripe.apiKey = stripeApiKey;
    }

    // ── Stripe payment ────────────────────────────────────────────────────────

    @PostMapping("/intent")
    public Map<String, String> createIntent(@RequestBody Map<String, Integer> body,
                                             HttpServletRequest request) throws StripeException {
        Long userId = extractUserId(request);
        Integer planId = body.get("planId");

        Plan plan = planRepository.findById(planId)
            .orElseThrow(() -> new NotFoundException("Plan not found: " + planId));
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found: " + userId));

        String customerId = getOrCreateStripeCustomer(user);

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        int dayBit = 1 << (today.getDayOfWeek().getValue() - 1);
        var offer = offerRepository.findCandidateOffers(planId, today, now)
            .stream()
            .filter(o -> {
                Integer mask = o.getDaysOfWeek();
                return mask == null || mask == 0 || (mask & dayBit) != 0;
            })
            .findFirst();

        BigDecimal originalPrice = plan.getPrice();
        BigDecimal finalPrice = offer
            .map(o -> originalPrice.multiply(BigDecimal.valueOf(100 - o.getDiscountPercent()))
                                   .divide(BigDecimal.valueOf(100)))
            .orElse(originalPrice);

        long amountCents = finalPrice.multiply(BigDecimal.valueOf(100)).longValue();

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
            .setAmount(amountCents)
            .setCurrency("mxn")
            .setCustomer(customerId)
            .putMetadata("planId", String.valueOf(planId))
            .putMetadata("userId", String.valueOf(userId))
            .setAutomaticPaymentMethods(
                PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                    .setEnabled(true)
                    .build()
            )
            .build();

        PaymentIntent intent = PaymentIntent.create(params);

        Map<String, String> response = new HashMap<>();
        response.put("clientSecret", intent.getClientSecret());
        response.put("paymentIntentId", intent.getId());
        offer.ifPresent(o -> {
            response.put("discountPercent", String.valueOf(o.getDiscountPercent()));
            response.put("originalPrice", originalPrice.toPlainString());
            response.put("finalPrice", finalPrice.toPlainString());
        });
        return response;
    }

    // ── Stripe client-side confirm (called by frontend after Stripe.js succeeds) ─

    @PostMapping("/stripe/confirm")
    public PaymentResponseDto confirmStripePayment(@RequestBody Map<String, Object> body,
                                                    HttpServletRequest request) {
        Long userId = extractUserId(request);
        String paymentIntentId = (String) body.get("paymentIntentId");
        Integer planId = ((Number) body.get("planId")).intValue();

        Plan plan = planRepository.findById(planId)
            .orElseThrow(() -> new NotFoundException("Plan not found: " + planId));

        PaymentInput paymentInput = new PaymentInput();
        paymentInput.setUserId(userId);
        paymentInput.setPlanId(planId);
        paymentInput.setAmount(plan.getPrice());
        paymentInput.setCurrency("MXN");
        paymentInput.setMethod(com.is.in_studio.entity.Payment.PaymentMethod.STRIPE);
        paymentInput.setTransactionRef(paymentIntentId);

        return paymentService.create(paymentInput);
    }

    // ── Manual payment (Cash / Bank Transfer) ────────────────────────────────

    @PostMapping("/manual")
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponseDto createManual(@RequestBody Map<String, Object> body,
                                            HttpServletRequest request) {
        Long userId = extractUserId(request);
        Integer planId = ((Number) body.get("planId")).intValue();
        String rawMethod = (String) body.get("paymentMethod");

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found: " + userId));
        Plan plan = planRepository.findById(planId)
            .orElseThrow(() -> new NotFoundException("Plan not found: " + planId));

        com.is.in_studio.entity.Payment.PaymentMethod method =
            "CASH".equalsIgnoreCase(rawMethod)
                ? com.is.in_studio.entity.Payment.PaymentMethod.CASH
                : com.is.in_studio.entity.Payment.PaymentMethod.TRANSFER;

        PaymentInput paymentInput = new PaymentInput();
        paymentInput.setUserId(userId);
        paymentInput.setPlanId(planId);
        paymentInput.setAmount(plan.getPrice());
        paymentInput.setCurrency("MXN");
        paymentInput.setMethod(method);
        var payment = paymentService.create(paymentInput);

        notifyAdmins(user, plan, rawMethod);
        return payment;
    }

    // ── Exception handler ─────────────────────────────────────────────────────

    @ExceptionHandler(StripeException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public Map<String, String> handleStripeException(StripeException e) {
        return Map.of("message", e.getMessage() != null ? e.getMessage() : "Payment processing error");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String getOrCreateStripeCustomer(User user) throws StripeException {
        if (user.getStripeCustomerId() != null) return user.getStripeCustomerId();
        CustomerCreateParams params = CustomerCreateParams.builder()
            .setEmail(user.getEmail())
            .setName(user.getFirstName() + " " + user.getLastName())
            .build();
        Customer customer = Customer.create(params);
        user.setStripeCustomerId(customer.getId());
        userRepository.save(user);
        return customer.getId();
    }

    private void notifyAdmins(User user, Plan plan, String method) {
        List<User> admins = userRepository.findByRole(User.UserRole.ADMIN);
        String methodLabel = "CASH".equals(method) ? "Cash" : "Bank Transfer";
        String subject = "Pending payment confirmation — " + plan.getName();
        String html = """
            <h2>Pending payment confirmation</h2>
            <p><b>%s %s</b> (%s) has registered a payment via <b>%s</b>.</p>
            <table>
              <tr><td><b>Plan:</b></td><td>%s</td></tr>
              <tr><td><b>Amount:</b></td><td>$%.2f MXN</td></tr>
              <tr><td><b>Method:</b></td><td>%s</td></tr>
            </table>
            <p>Please confirm receipt of payment to activate their membership.</p>
            """.formatted(
                user.getFirstName(), user.getLastName(), user.getEmail(),
                methodLabel, plan.getName(), plan.getPrice(), methodLabel);

        for (User admin : admins) {
            try {
                emailService.sendHtmlEmail(admin.getEmail(), subject, html);
            } catch (Exception ignored) {}
        }
    }

    private Long extractUserId(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        return jwtUtil.extractUserId(header.substring(7));
    }
}
