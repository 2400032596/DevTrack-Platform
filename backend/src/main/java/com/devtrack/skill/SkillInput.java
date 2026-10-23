package com.devtrack.skill;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SkillInput(
    @NotBlank @Size(max = 100) String name,
    @NotBlank @Size(max = 80) String category,
    @Min(1) @Max(5) int proficiencyLevel,
    @Min(1) @Max(5) int targetLevel
) {}
