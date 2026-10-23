package com.devtrack.common;

import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class CurrentUser {
  private CurrentUser() {}

  public static UUID id() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
      throw new ApiException(
          org.springframework.http.HttpStatus.UNAUTHORIZED,
          "UNAUTHORIZED",
          "Authentication is required.");
    }
    return principal.id();
  }
}
