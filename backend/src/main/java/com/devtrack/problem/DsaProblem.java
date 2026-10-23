package com.devtrack.problem;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "dsa_problems")
public class DsaProblem {
  @Id
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(nullable = false, length = 200)
  private String title;

  @Column(nullable = false, length = 80)
  private String topic;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 12)
  private Difficulty difficulty;

  @Column(nullable = false, length = 80)
  private String platform;

  @Column(name = "problem_url", length = 2048)
  private String problemUrl;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private ProblemStatus status;

  @Column(columnDefinition = "text")
  private String notes;

  @Column(name = "solved_at")
  private Instant solvedAt;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected DsaProblem() {}

  public DsaProblem(UUID userId, ProblemInput input) {
    this.id = UUID.randomUUID();
    this.userId = userId;
    update(input);
  }

  public void update(ProblemInput input) {
    title = input.title().trim();
    topic = input.topic().trim();
    difficulty = input.difficulty();
    platform = input.platform().trim();
    problemUrl = blankToNull(input.problemUrl());
    status = input.status();
    notes = blankToNull(input.notes());
    solvedAt = input.solvedAt();
    if (status == ProblemStatus.SOLVED && solvedAt == null) solvedAt = Instant.now();
    if (status != ProblemStatus.SOLVED) solvedAt = null;
  }

  private String blankToNull(String value) {
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
  public String getTopic() { return topic; }
  public Difficulty getDifficulty() { return difficulty; }
  public String getPlatform() { return platform; }
  public String getProblemUrl() { return problemUrl; }
  public ProblemStatus getStatus() { return status; }
  public String getNotes() { return notes; }
  public Instant getSolvedAt() { return solvedAt; }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
}
