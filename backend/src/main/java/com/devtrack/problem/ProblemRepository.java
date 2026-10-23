package com.devtrack.problem;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProblemRepository extends JpaRepository<DsaProblem, UUID> {
  Optional<DsaProblem> findByIdAndUserId(UUID id, UUID userId);
  long countByUserId(UUID userId);
  long countByUserIdAndStatus(UUID userId, ProblemStatus status);
  long countByUserIdAndDifficulty(UUID userId, Difficulty difficulty);
  long countByUserIdAndDifficultyAndStatus(UUID userId, Difficulty difficulty, ProblemStatus status);
  List<DsaProblem> findAllByUserId(UUID userId);

  @Query("""
      select p from DsaProblem p
      where p.userId = :userId
        and (:search is null or lower(p.title) like lower(concat('%', :search, '%')))
        and (:topic is null or lower(p.topic) = lower(:topic))
        and (:difficulty is null or p.difficulty = :difficulty)
        and (:status is null or p.status = :status)
      """)
  Page<DsaProblem> searchForUser(
      @Param("userId") UUID userId,
      @Param("search") String search,
      @Param("topic") String topic,
      @Param("difficulty") Difficulty difficulty,
      @Param("status") ProblemStatus status,
      Pageable pageable);

  @Query("select p.topic as topic, count(p) as total, sum(case when p.status = com.devtrack.problem.ProblemStatus.SOLVED then 1 else 0 end) as solved from DsaProblem p where p.userId = :userId group by p.topic order by p.topic")
  List<TopicProgressProjection> topicProgress(@Param("userId") UUID userId);

  interface TopicProgressProjection {
    String getTopic();
    long getTotal();
    long getSolved();
  }
}
