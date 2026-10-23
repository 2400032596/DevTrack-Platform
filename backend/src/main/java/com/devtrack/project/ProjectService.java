package com.devtrack.project;

import com.devtrack.activity.ActivityService;
import com.devtrack.common.ApiException;
import com.devtrack.common.PageResponse;
import com.devtrack.common.UrlRules;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectService {
  private static final Set<String> SORT_FIELDS =
      Set.of("createdAt", "startDate", "targetDate", "title");
  private final ProjectRepository projects;
  private final ActivityService activity;

  public ProjectService(ProjectRepository projects, ActivityService activity) {
    this.projects = projects;
    this.activity = activity;
  }

  @Transactional(readOnly = true)
  public PageResponse<ProjectResponse> list(
      UUID userId, int page, int size, String search, ProjectStatus status, String sort, String direction) {
    if (!SORT_FIELDS.contains(sort)) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_SORT", "Unsupported project sort field.");
    }
    Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction)
        ? Sort.Direction.ASC : Sort.Direction.DESC;
    Page<Project> result = projects.searchForUser(
        userId,
        clean(search),
        status,
        PageRequest.of(page, size, Sort.by(sortDirection, sort)));
    return PageResponse.from(result, ProjectResponse::from);
  }

  @Transactional(readOnly = true)
  public ProjectResponse get(UUID userId, UUID id) {
    return ProjectResponse.from(findOwned(userId, id));
  }

  @Transactional
  public ProjectResponse create(UUID userId, ProjectInput input) {
    validate(input);
    Project project = projects.save(new Project(userId, input));
    activity.record(userId, "CREATED", "PROJECT", project.getId());
    return ProjectResponse.from(project);
  }

  @Transactional
  public ProjectResponse update(UUID userId, UUID id, ProjectInput input) {
    validate(input);
    Project project = findOwned(userId, id);
    project.update(input);
    Project saved = projects.save(project);
    activity.record(userId, "UPDATED", "PROJECT", id);
    return ProjectResponse.from(saved);
  }

  @Transactional
  public void delete(UUID userId, UUID id) {
    Project project = findOwned(userId, id);
    activity.record(userId, "DELETED", "PROJECT", id);
    projects.delete(project);
  }

  private void validate(ProjectInput input) {
    UrlRules.validateOptionalHttpUrl(input.repositoryUrl(), "repositoryUrl");
    UrlRules.validateOptionalHttpUrl(input.demoUrl(), "demoUrl");
    if (input.startDate() != null && input.targetDate() != null
        && input.targetDate().isBefore(input.startDate())) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Target date must be on or after the start date.");
    }
  }

  private Project findOwned(UUID userId, UUID id) {
    return projects.findByIdAndUserId(id, userId)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Project not found."));
  }

  private String clean(String value) {
    return value == null || value.isBlank() ? null : value.trim();
  }
}
