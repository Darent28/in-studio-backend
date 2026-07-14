package com.is.in_studio.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.is.in_studio.domain.dto.AuthResponseDto;
import com.is.in_studio.domain.input.LoginInput;
import com.is.in_studio.domain.input.UserInput;
import com.is.in_studio.entity.User;
import com.is.in_studio.exception.CustomExceptions.ProcessServiceException;
import com.is.in_studio.repository.UserRepository;
import com.is.in_studio.service.AuthService;
import com.is.in_studio.service.EmailConfirmationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final EmailConfirmationService emailConfirmationService;
    private final UserRepository userRepository;

    public AuthController(AuthService authService, EmailConfirmationService emailConfirmationService,
                          UserRepository userRepository) {
        this.authService = authService;
        this.emailConfirmationService = emailConfirmationService;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public AuthResponseDto login(@Valid @RequestBody LoginInput input) {
        return authService.login(input);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponseDto register(@Valid @RequestBody UserInput input) {
        return authService.register(input);
    }

    @PostMapping("/resend-confirmation")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void resendConfirmation(@RequestParam String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ProcessServiceException("No account found with that email."));
        emailConfirmationService.sendConfirmationEmail(user);
    }

    @GetMapping("/confirm-email")
    public ResponseEntity<String> confirmEmail(@RequestParam String token) {
        emailConfirmationService.confirmEmail(token);
        String html = """
            <!DOCTYPE html>
            <html>
            <body style="margin:0;padding:0;background-color:#f3f0ff;font-family:Arial,sans-serif;">
              <div style="max-width:480px;margin:60px auto;background:#ffffff;border-radius:12px;
                          box-shadow:0 4px 20px rgba(109,40,217,0.12);padding:48px 40px;text-align:center;">
                <div style="width:64px;height:64px;background:#7c3aed;border-radius:50%;
                            margin:0 auto 24px;line-height:64px;font-size:32px;color:#ffffff;">
                  &#10003;
                </div>
                <h2 style="color:#5b21b6;margin:0 0 12px;font-size:24px;">Email Confirmed!</h2>
                <p style="color:#6b7280;font-size:15px;line-height:1.6;margin:0 0 32px;">
                  Your email has been verified successfully.<br>You can close this page and return to the app.
                </p>
                <div style="width:48px;height:3px;background:linear-gradient(90deg,#7c3aed,#a78bfa);
                            border-radius:2px;margin:0 auto;"></div>
              </div>
            </body>
            </html>
            """;
        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(html);
    }
}
