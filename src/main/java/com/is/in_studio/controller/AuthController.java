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
            <html><body style="font-family: Arial, sans-serif; text-align: center; padding: 50px;">
            <h2 style="color: #4c66af;">Email Confirmed!</h2>
            <p>Your email has been verified successfully. You can close this page and return to the app.</p>
            </body></html>
            """;
        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(html);
    }
}
