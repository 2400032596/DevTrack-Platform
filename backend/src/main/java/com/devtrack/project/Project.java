package com.devtrack.project;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "projects")
public class Project {
  @Id
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(nullable = false, length = 160)
  private String title;

  @Column(columnDefinition = "text")
  private String description;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "project_technologies", joinColumns = @JoinColumn(name = "project_id"))
  @Column(name = "technology", nullable = false, length = 40)
  private List<String> technologies = new ArrayList<>();

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private ProjectStatus status;

  @Column(name = "repository_url", length = 2048)
  private String repositoryUrl;

  @Column(name = "demo_url", length = 2048)
  private String demoUrl;

  @Column(name = "start_date")
  private LocalDate startDate;

  @Column(name = "target_date")
  private LocalDate targetDate;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected Project() {}

  public Project(UUID userId, ProjectInput input) {
    id = UUID.randomUUID();
    this.userId = userId;
    update(input);
  }

  public void update(ProjectInput input) {
    title = input.title().trim();
    description = clean(input.description());
    technologies.clear();
    input.technologies().stream()
        .map(String::trim).filter(value -> !value.isEmpty()).distinct()
        .forEach(technologies::add);
    status = input.status();
    repositoryUrl = clean(input.repositoryUrl());
    demoUrl = clean(input.demoUrl());
    startDate = input.startDate();
    targetDate = input.targetDate();
  }

  private String clean(String value) {
    return value == null || value.isBlank() ? null : value.trim();
  }

  @PrePersist
  void onCreate() {
    Instant now = Instant.now();
    createdAt = now;
    updatedAt = now;
  }

  @PreUpdate
  void onUpdate() { updatedAt = Instant.now(); }

  public UUID getId() { return id; }
  public UUID getUserId() { return userId; }
  public String getTitle() { return title; }
  public String getDescription() { return description; }
  public List<String> getTechnologies() { return List.copyOf(technologies); }
  public ProjectStatus getStatus() { return status; }
  public String getRepositoryUrl() { return repositoryUrl; }
  public String getDemoUrl() { return demoUrl; }
  public LocalDate getStartDate() { return startDate; }
  public LocalDate getTargetDate() { return targetDate; }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
}
