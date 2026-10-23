package com.devtrack.activity;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ActivityService {
  private final ActivityLogRepository repository;

  public ActivityService(ActivityLogRepository repository) {
    this.repository = repository;
  }

  public void record(UUID userId, String action, String entityType, UUID entityId) {
    repository.save(new ActivityLog(userId, action, entityType, entityId));
  }

  @Transactional(readOnly = true)
  public List<ActivityResponse> recent(UUID userId) {
    return repository.findTop10ByUserIdOrderByCreatedAtDesc(userId).stream()
        .map(ActivityResponse::from).toList();
  }

  @Transactional(readOnly = true)
  public List<ActivityDay> dailyCounts(UUID userId, int days) {
    LocalDate today = LocalDate.now(ZoneOffset.UTC);
    LocalDate firstDate = today.minusDays(days - 1L);
    Instant start = firstDate.atStartOfDay(ZoneOffset.UTC).toInstant();
    Map<LocalDate, Integer> counts = new HashMap<>();
    for (ActivityLog activity : repository.since(userId, start)) {
      LocalDate date = activity.getCreatedAt().atZone(ZoneOffset.UTC).toLocalDate();
      counts.merge(date, 1, Integer::sum);
    }
    List<ActivityDay> result = new ArrayList<>(days);
    for (int i = 0; i < days; i++) {
      LocalDate date = firstDate.plusDays(i);
      result.add(new ActivityDay(date, counts.getOrDefault(date, 0)));
    }
    return result;
  }

  public record ActivityDay(LocalDate date, int count) {}
}
