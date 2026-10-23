package com.devtrack.problem;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record ProblemInput(
    @NotBlank @Size(max = 200) String title,
    @NotBlank @Size(max = 80) String topic,
    @NotNull Difficulty difficulty,
    @NotBlank @Size(max = 80) String platform,
    @Size(max = 2048) String problemUrl,
    @NotNull ProblemStatus status,
    @Size(max = 5000) String notes,
    Instant solvedAt
) {}
