package com.devtrack.auth;

import com.devtrack.user.UserAccount;
import java.time.Instant;
import java.util.UUID;

public record AuthUserResponse(UUID id, String name, String email, Instant createdAt) {
  public static AuthUserResponse from(UserAccount user) {
    return new AuthUserResponse(user.getId(), user.getName(), user.getEmail(), user.getCreatedAt());
  }
}
