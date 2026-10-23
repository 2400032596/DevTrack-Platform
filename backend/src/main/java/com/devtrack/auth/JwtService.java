package com.devtrack.auth;

import com.devtrack.user.UserAccount;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
  private final SecretKey signingKey;
  private final long accessTokenMinutes;

  public JwtService(
      @Value("${devtrack.security.jwt-secret}") String secret,
      @Value("${devtrack.security.access-token-minutes:15}") long accessTokenMinutes) {
    if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
      throw new IllegalStateException("JWT_SECRET (or SESSION_SECRET) must contain at least 32 bytes.");
    }
    this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.accessTokenMinutes = accessTokenMinutes;
  }

  public String issue(UserAccount user) {
    Instant now = Instant.now();
    return Jwts.builder()
        .subject(user.getEmail())
        .claim("uid", user.getId().toString())
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plusSeconds(accessTokenMinutes * 60)))
        .signWith(signingKey)
        .compact();
  }

  public Claims parse(String token) {
    return Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload();
  }

  public UUID userId(String token) {
    return UUID.fromString(parse(token).get("uid", String.class));
  }

  public String subject(String token) {
    return parse(token).getSubject();
  }
}
