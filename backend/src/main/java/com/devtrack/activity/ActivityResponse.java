package com.devtrack.activity;

import java.time.Instant;
import java.util.UUID;

public record ActivityResponse(
    UUID id,
    String actionType,
    String entityType,
    UUID entityId,
    Instant createdAt
) {
  public static ActivityResponse from(ActivityLog log) {
    return new ActivityResponse(
        log.getId(), log.getActionType(), log.getEntityType(), log.getEntityId(), log.getCreatedAt());
  }
}
