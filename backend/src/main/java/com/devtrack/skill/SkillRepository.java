package com.devtrack.skill;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SkillRepository extends JpaRepository<Skill, UUID> {
  Optional<Skill> findByIdAndUserId(UUID id, UUID userId);
  List<Skill> findAllByUserId(UUID userId);
  long countByUserId(UUID userId);
  long countByUserIdAndProficiencyLevelGreaterThanEqual(UUID userId, int targetLevel);

  @Query("""
      select s from Skill s where s.userId = :userId
        and (:category is null or lower(s.category) = lower(:category))
        and (:search is null or lower(s.name) like lower(concat('%', :search, '%')))
      """)
  List<Skill> searchForUser(
      @Param("userId") UUID userId,
      @Param("category") String category,
      @Param("search") String search);
}
