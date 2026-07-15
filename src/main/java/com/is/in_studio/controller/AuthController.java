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
import com.is.in_studio.service.PasswordResetService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final EmailConfirmationService emailConfirmationService;
    private final PasswordResetService passwordResetService;
    private final UserRepository userRepository;

    public AuthController(AuthService authService, EmailConfirmationService emailConfirmationService,
                          PasswordResetService passwordResetService, UserRepository userRepository) {
        this.authService = authService;
        this.emailConfirmationService = emailConfirmationService;
        this.passwordResetService = passwordResetService;
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

    @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void forgotPassword(@RequestParam String email) {
        passwordResetService.sendResetEmail(email);
    }

    @GetMapping("/reset-password")
    public ResponseEntity<String> resetPasswordPage(@RequestParam String token) {
        String html = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
              <meta charset="UTF-8">
              <meta name="viewport" content="width=device-width, initial-scale=1.0">
              <title>Reset Password — In Studio</title>
              <style>
                *{box-sizing:border-box;margin:0;padding:0}
                body{background:#f3f0ff;font-family:Arial,sans-serif;min-height:100vh;
                     display:flex;align-items:center;justify-content:center;padding:20px}
                .card{background:#fff;border-radius:12px;box-shadow:0 4px 20px rgba(109,40,217,.12);
                      padding:48px 40px;width:100%%;max-width:420px}
                h2{color:#5b21b6;font-size:22px;margin-bottom:8px}
                .sub{color:#6b7280;font-size:14px;margin-bottom:28px}
                label{display:block;font-size:13px;font-weight:bold;color:#374151;margin-bottom:6px}
                input{width:100%%;padding:11px 14px;border:1.5px solid #d1d5db;border-radius:8px;
                      font-size:15px;outline:none;transition:border-color .2s}
                input:focus{border-color:#7c3aed}
                .field{margin-bottom:18px}
                .hint{font-size:12px;margin-top:5px;min-height:16px}
                .hint.error{color:#dc2626}
                .hint.ok{color:#16a34a}
                button{width:100%%;padding:13px;background:#7c3aed;color:#fff;border:none;
                       border-radius:8px;font-size:15px;font-weight:bold;cursor:pointer;
                       transition:background .2s;margin-top:4px}
                button:hover:not(:disabled){background:#6d28d9}
                button:disabled{opacity:.6;cursor:not-allowed}
                .banner{display:none;padding:12px 16px;border-radius:8px;font-size:14px;
                        margin-bottom:20px;text-align:center}
                .banner.success{background:#ede9fe;color:#5b21b6;display:block}
                .banner.error{background:#fee2e2;color:#dc2626;display:block}
                .divider{border:none;border-top:2px solid #ede9fe;margin:28px 0 0}
              </style>
            </head>
            <body>
              <div class="card">
                <h2>Reset your password</h2>
                <p class="sub">Enter your new password below.</p>

                <div id="banner" class="banner"></div>

                <form id="form" novalidate>
                  <input type="hidden" id="token" value="%s">

                  <div class="field">
                    <label for="newPassword">New password</label>
                    <input type="password" id="newPassword" placeholder="At least 8 characters" autocomplete="new-password">
                    <p class="hint" id="hintNew"></p>
                  </div>

                  <div class="field">
                    <label for="confirmPassword">Confirm password</label>
                    <input type="password" id="confirmPassword" placeholder="Repeat your password" autocomplete="new-password">
                    <p class="hint" id="hintConfirm"></p>
                  </div>

                  <button type="submit" id="btn">Reset Password</button>
                </form>
                <hr class="divider">
              </div>

              <script>
                const newPwd   = document.getElementById('newPassword');
                const confPwd  = document.getElementById('confirmPassword');
                const hintNew  = document.getElementById('hintNew');
                const hintConf = document.getElementById('hintConfirm');
                const btn      = document.getElementById('btn');
                const banner   = document.getElementById('banner');

                function validateNew() {
                  if (newPwd.value.length === 0) { hintNew.textContent = ''; hintNew.className = 'hint'; return false; }
                  if (newPwd.value.length < 8) {
                    hintNew.textContent = 'At least 8 characters required.';
                    hintNew.className = 'hint error'; return false;
                  }
                  hintNew.textContent = 'Looks good!';
                  hintNew.className = 'hint ok'; return true;
                }

                function validateConfirm() {
                  if (confPwd.value.length === 0) { hintConf.textContent = ''; hintConf.className = 'hint'; return false; }
                  if (newPwd.value !== confPwd.value) {
                    hintConf.textContent = 'Passwords do not match.';
                    hintConf.className = 'hint error'; return false;
                  }
                  hintConf.textContent = 'Passwords match!';
                  hintConf.className = 'hint ok'; return true;
                }

                newPwd.addEventListener('input', () => { validateNew(); if (confPwd.value) validateConfirm(); });
                confPwd.addEventListener('input', validateConfirm);

                document.getElementById('form').addEventListener('submit', async (e) => {
                  e.preventDefault();
                  if (!validateNew() || !validateConfirm()) return;

                  btn.disabled = true;
                  btn.textContent = 'Resetting…';
                  banner.className = 'banner';

                  try {
                    const res = await fetch('/api/auth/reset-password', {
                      method: 'POST',
                      headers: { 'Content-Type': 'application/json' },
                      body: JSON.stringify({
                        token: document.getElementById('token').value,
                        newPassword: newPwd.value,
                        confirmPassword: confPwd.value
                      })
                    });

                    if (res.ok) {
                      banner.textContent = 'Password updated! You can now log in with your new password.';
                      banner.className = 'banner success';
                      document.getElementById('form').style.display = 'none';
                    } else {
                      const data = await res.json().catch(() => ({}));
                      banner.textContent = data.detail || 'Something went wrong. Please try again.';
                      banner.className = 'banner error';
                      btn.disabled = false;
                      btn.textContent = 'Reset Password';
                    }
                  } catch {
                    banner.textContent = 'Network error. Please try again.';
                    banner.className = 'banner error';
                    btn.disabled = false;
                    btn.textContent = 'Reset Password';
                  }
                });
              </script>
            </body>
            </html>
            """.formatted(token);
        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(html);
    }

    @PostMapping("/reset-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(@RequestBody ResetPasswordRequest body) {
        passwordResetService.resetPassword(body.token(), body.newPassword(), body.confirmPassword());
    }

    record ResetPasswordRequest(String token, String newPassword, String confirmPassword) {}
}
