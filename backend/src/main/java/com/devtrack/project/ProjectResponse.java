package com.devtrack.project;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ProjectResponse(
    UUID id,
    String title,
    String description,
    List<String> technologies,
    ProjectStatus status,
    String repositoryUrl,
    String demoUrl,
    LocalDate startDate,
    LocalDate targetDate,
    Instant createdAt,
    Instant updatedAt
) {
  public static ProjectResponse from(Project project) {
    return new ProjectResponse(
        project.getId(), project.getTitle(), project.getDescription(), project.getTechnologies(),
        project.getStatus(), project.getRepositoryUrl(), project.getDemoUrl(), project.getStartDate(),
        project.getTargetDate(), project.getCreatedAt(), project.getUpdatedAt());
  }
}
