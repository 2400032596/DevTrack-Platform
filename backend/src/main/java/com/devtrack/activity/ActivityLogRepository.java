package com.devtrack.activity;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, UUID> {
  List<ActivityLog> findTop10ByUserIdOrderByCreatedAtDesc(UUID userId);

  @Query("""
      select a from ActivityLog a
      where a.userId = :userId and a.createdAt >= :from
      order by a.createdAt asc
      """)
  List<ActivityLog> since(@Param("userId") UUID userId, @Param("from") Instant from);
}
