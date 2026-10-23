package com.devtrack.problem;

import java.time.Instant;
import java.util.UUID;

public record ProblemResponse(
    UUID id,
    String title,
    String topic,
    Difficulty difficulty,
    String platform,
    String problemUrl,
    ProblemStatus status,
    String notes,
    Instant solvedAt,
    Instant createdAt,
    Instant updatedAt
) {
  public static ProblemResponse from(DsaProblem problem) {
    return new ProblemResponse(
        problem.getId(), problem.getTitle(), problem.getTopic(), problem.getDifficulty(),
        problem.getPlatform(), problem.getProblemUrl(), problem.getStatus(), problem.getNotes(),
        problem.getSolvedAt(), problem.getCreatedAt(), problem.getUpdatedAt());
  }
}
