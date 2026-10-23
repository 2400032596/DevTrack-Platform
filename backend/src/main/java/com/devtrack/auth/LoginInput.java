package com.devtrack.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginInput(
    @NotBlank @Email @Size(max = 254) String email,
    @NotBlank @Size(max = 72) String password
) {}
