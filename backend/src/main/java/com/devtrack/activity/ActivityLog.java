package com.devtrack.activity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "activity_logs")
public class ActivityLog {
  @Id
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "action_type", nullable = false, length = 32)
  private String actionType;

  @Column(name = "entity_type", nullable = false, length = 32)
  private String entityType;

  @Column(name = "entity_id")
  private UUID entityId;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected ActivityLog() {}

  public ActivityLog(UUID userId, String actionType, String entityType, UUID entityId) {
    id = UUID.randomUUID();
    this.userId = userId;
    this.actionType = actionType;
    this.entityType = entityType;
    this.entityId = entityId;
    createdAt = Instant.now();
  }

  public UUID getId() { return id; }
  public UUID getUserId() { return userId; }
  public String getActionType() { return actionType; }
  public String getEntityType() { return entityType; }
  public UUID getEntityId() { return entityId; }
  public Instant getCreatedAt() { return createdAt; }
}
