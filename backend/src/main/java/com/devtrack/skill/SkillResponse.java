package com.devtrack.skill;

import java.time.Instant;
import java.util.UUID;

public record SkillResponse(
    UUID id,
    String name,
    String category,
    int proficiencyLevel,
    int targetLevel,
    Instant createdAt,
    Instant updatedAt
) {
  public static SkillResponse from(Skill skill) {
    return new SkillResponse(
        skill.getId(), skill.getName(), skill.getCategory(), skill.getProficiencyLevel(),
        skill.getTargetLevel(), skill.getCreatedAt(), skill.getUpdatedAt());
  }
}
