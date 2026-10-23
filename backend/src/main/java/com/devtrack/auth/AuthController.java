package com.devtrack.auth;

import com.devtrack.common.CurrentUser;
import com.devtrack.common.ApiException;
import com.devtrack.user.UserAccount;
import com.devtrack.user.UserRepository;
import jakarta.validation.Valid;
import java.time.Duration;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth")
public class AuthController {
  private final AuthService authService;
  private final UserRepository users;
  private final boolean secureCookie;
  private final String sameSite;
  private final long accessTokenMinutes;

  public AuthController(
      AuthService authService,
      UserRepository users,
      @Value("${devtrack.security.cookie-secure:false}") boolean secureCookie,
      @Value("${devtrack.security.cookie-same-site:Strict}") String sameSite,
      @Value("${devtrack.security.access-token-minutes:15}") long accessTokenMinutes) {
    this.authService = authService;
    this.users = users;
    this.secureCookie = secureCookie;
    this.sameSite = sameSite;
    this.accessTokenMinutes = accessTokenMinutes;
  }

  @PostMapping("/register")
  public ResponseEntity<AuthUserResponse> register(@Valid @RequestBody RegisterInput input) {
    AuthService.AuthResult result = authService.register(input);
    return ResponseEntity.status(HttpStatus.CREATED)
        .header("Set-Cookie", accessCookie(result.token(), Duration.ofMinutes(accessTokenMinutes)).toString())
        .body(result.user());
  }

  @PostMapping("/login")
  public ResponseEntity<AuthUserResponse> login(@Valid @RequestBody LoginInput input) {
    AuthService.AuthResult result = authService.login(input);
    return ResponseEntity.ok()
        .header("Set-Cookie", accessCookie(result.token(), Duration.ofMinutes(accessTokenMinutes)).toString())
        .body(result.user());
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout() {
    return ResponseEntity.noContent()
        .header("Set-Cookie", accessCookie("", Duration.ZERO).toString())
        .build();
  }

  @GetMapping("/me")
  public AuthUserResponse me() {
    UUID userId = CurrentUser.id();
    UserAccount user = users.findById(userId)
        .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Authentication is required."));
    return AuthUserResponse.from(user);
  }

  private ResponseCookie accessCookie(String token, Duration maxAge) {
    return ResponseCookie.from("DEVTRACK_ACCESS", token)
        .httpOnly(true)
        .secure(secureCookie)
        .sameSite(sameSite)
        .path("/api/v1")
        .maxAge(maxAge)
        .build();
  }
}
