package com.devtrack.skill;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "skills")
public class Skill {
  @Id
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(nullable = false, length = 100)
  private String name;

  @Column(nullable = false, length = 80)
  private String category;

  @Column(name = "proficiency_level", nullable = false)
  private int proficiencyLevel;

  @Column(name = "target_level", nullable = false)
  private int targetLevel;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected Skill() {}

  public Skill(UUID userId, SkillInput input) {
    id = UUID.randomUUID();
    this.userId = userId;
    update(input);
  }

  public void update(SkillInput input) {
    name = input.name().trim();
    category = input.category().trim();
    proficiencyLevel = input.proficiencyLevel();
    targetLevel = input.targetLevel();
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
  public String getName() { return name; }
  public String getCategory() { return category; }
  public int getProficiencyLevel() { return proficiencyLevel; }
  public int getTargetLevel() { return targetLevel; }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
}
