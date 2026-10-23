package com.devtrack.common;

import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;

public final class UrlRules {
  private UrlRules() {}

  public static void validateOptionalHttpUrl(String value, String field) {
    if (value == null || value.isBlank()) return;
    try {
      URI uri = URI.create(value.trim());
      if (!List.of("http", "https").contains(uri.getScheme()) || uri.getHost() == null) {
        throw new IllegalArgumentException();
      }
    } catch (IllegalArgumentException exception) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", field + " must be a valid http or https URL.");
    }
  }
}
