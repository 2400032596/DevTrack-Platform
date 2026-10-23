package com.devtrack.skill;

import com.devtrack.activity.ActivityService;
import com.devtrack.common.ApiException;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SkillService {
  private final SkillRepository skills;
  private final ActivityService activity;

  public SkillService(SkillRepository skills, ActivityService activity) {
    this.skills = skills;
    this.activity = activity;
  }

  @Transactional(readOnly = true)
  public List<SkillResponse> list(UUID userId, String category, String search, String sort, String direction) {
    if (!Set.of("name", "category", "proficiencyLevel", "updatedAt").contains(sort)) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_SORT", "Unsupported skill sort field.");
    }
    Comparator<Skill> comparator = switch (sort) {
      case "category" -> Comparator.comparing(Skill::getCategory, String.CASE_INSENSITIVE_ORDER);
      case "proficiencyLevel" -> Comparator.comparingInt(Skill::getProficiencyLevel);
      case "updatedAt" -> Comparator.comparing(Skill::getUpdatedAt);
      default -> Comparator.comparing(Skill::getName, String.CASE_INSENSITIVE_ORDER);
    };
    if ("desc".equalsIgnoreCase(direction)) comparator = comparator.reversed();
    return skills.searchForUser(userId, clean(category), clean(search)).stream()
        .sorted(comparator).map(SkillResponse::from).toList();
  }

  @Transactional
  public SkillResponse create(UUID userId, SkillInput input) {
    Skill skill = skills.save(new Skill(userId, input));
    activity.record(userId, "CREATED", "SKILL", skill.getId());
    return SkillResponse.from(skill);
  }

  @Transactional
  public SkillResponse update(UUID userId, UUID id, SkillInput input) {
    Skill skill = findOwned(userId, id);
    skill.update(input);
    Skill saved = skills.save(skill);
    activity.record(userId, "UPDATED", "SKILL", id);
    return SkillResponse.from(saved);
  }

  @Transactional
  public void delete(UUID userId, UUID id) {
    Skill skill = findOwned(userId, id);
    activity.record(userId, "DELETED", "SKILL", id);
    skills.delete(skill);
  }

  private Skill findOwned(UUID userId, UUID id) {
    return skills.findByIdAndUserId(id, userId)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Skill not found."));
  }

  private String clean(String value) {
    return value == null || value.isBlank() ? null : value.trim();
  }
}
