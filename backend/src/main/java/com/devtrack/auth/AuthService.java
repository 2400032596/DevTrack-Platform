package com.devtrack.auth;

import com.devtrack.common.ApiException;
import com.devtrack.user.UserAccount;
import com.devtrack.user.UserRepository;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
  private final UserRepository users;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;

  public AuthService(
      UserRepository users,
      PasswordEncoder passwordEncoder,
      AuthenticationManager authenticationManager,
      JwtService jwtService) {
    this.users = users;
    this.passwordEncoder = passwordEncoder;
    this.authenticationManager = authenticationManager;
    this.jwtService = jwtService;
  }

  @Transactional
  public AuthResult register(RegisterInput input) {
    String email = normalize(input.email());
    if (users.existsByEmail(email)) {
      throw new ApiException(HttpStatus.CONFLICT, "EMAIL_ALREADY_REGISTERED", "An account with this email already exists.");
    }
    UserAccount user = users.save(new UserAccount(
        input.name().trim(), email, passwordEncoder.encode(input.password())));
    return new AuthResult(AuthUserResponse.from(user), jwtService.issue(user));
  }

  @Transactional(readOnly = true)
  public AuthResult login(LoginInput input) {
    String email = normalize(input.email());
    try {
      authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(email, input.password()));
    } catch (AuthenticationException exception) {
      throw new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Email or password is incorrect.");
    }
    UserAccount user = users.findByEmail(email)
        .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Email or password is incorrect."));
    return new AuthResult(AuthUserResponse.from(user), jwtService.issue(user));
  }

  private String normalize(String email) {
    return email.trim().toLowerCase(Locale.ROOT);
  }

  public record AuthResult(AuthUserResponse user, String token) {}
}
