package com.devtrack.project;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectRepository extends JpaRepository<Project, UUID> {
  Optional<Project> findByIdAndUserId(UUID id, UUID userId);
  long countByUserId(UUID userId);
  long countByUserIdAndStatus(UUID userId, ProjectStatus status);

  @Query("""
      select p from Project p
      where p.userId = :userId
        and (:search is null or lower(p.title) like lower(concat('%', :search, '%')))
        and (:status is null or p.status = :status)
      """)
  Page<Project> searchForUser(
      @Param("userId") UUID userId,
      @Param("search") String search,
      @Param("status") ProjectStatus status,
      Pageable pageable);
}
