package com.devtrack.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

public record ProjectInput(
    @NotBlank @Size(max = 160) String title,
    @Size(max = 4000) String description,
    @NotNull @Size(max = 30) List<@NotBlank @Size(max = 40) String> technologies,
    @NotNull ProjectStatus status,
    @Size(max = 2048) String repositoryUrl,
    @Size(max = 2048) String demoUrl,
    LocalDate startDate,
    LocalDate targetDate
) {}
