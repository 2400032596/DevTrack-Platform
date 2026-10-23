package com.devtrack.problem;

import com.devtrack.activity.ActivityService;
import com.devtrack.common.ApiException;
import com.devtrack.common.PageResponse;
import com.devtrack.common.UrlRules;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProblemService {
  private final ProblemRepository problems;
  private final ActivityService activity;

  public ProblemService(ProblemRepository problems, ActivityService activity) {
    this.problems = problems;
    this.activity = activity;
  }

  @Transactional(readOnly = true)
  public PageResponse<ProblemResponse> list(
      UUID userId, int page, int size, String search, String topic,
      Difficulty difficulty, ProblemStatus status, String sort, String direction) {
    if (!java.util.Set.of("createdAt", "solvedAt", "title").contains(sort)) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_SORT", "Unsupported problem sort field.");
    }
    Sort.Direction sortDirection = parseDirection(direction);
    Page<DsaProblem> result = problems.searchForUser(
        userId, clean(search), clean(topic), difficulty, status,
        PageRequest.of(page, size, Sort.by(sortDirection, sort)));
    return PageResponse.from(result, ProblemResponse::from);
  }

  @Transactional(readOnly = true)
  public ProblemResponse get(UUID userId, UUID id) {
    return ProblemResponse.from(findOwned(userId, id));
  }

  @Transactional
  public ProblemResponse create(UUID userId, ProblemInput input) {
    UrlRules.validateOptionalHttpUrl(input.problemUrl(), "problemUrl");
    DsaProblem problem = problems.save(new DsaProblem(userId, input));
    activity.record(userId, input.status() == ProblemStatus.SOLVED ? "SOLVED" : "CREATED", "PROBLEM", problem.getId());
    return ProblemResponse.from(problem);
  }

  @Transactional
  public ProblemResponse update(UUID userId, UUID id, ProblemInput input) {
    UrlRules.validateOptionalHttpUrl(input.problemUrl(), "problemUrl");
    DsaProblem problem = findOwned(userId, id);
    ProblemStatus previousStatus = problem.getStatus();
    problem.update(input);
    DsaProblem saved = problems.save(problem);
    String action = previousStatus != ProblemStatus.SOLVED && input.status() == ProblemStatus.SOLVED
        ? "SOLVED" : "UPDATED";
    activity.record(userId, action, "PROBLEM", id);
    return ProblemResponse.from(saved);
  }

  @Transactional
  public void delete(UUID userId, UUID id) {
    DsaProblem problem = findOwned(userId, id);
    activity.record(userId, "DELETED", "PROBLEM", id);
    problems.delete(problem);
  }

  private DsaProblem findOwned(UUID userId, UUID id) {
    return problems.findByIdAndUserId(id, userId)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Problem not found."));
  }

  private Sort.Direction parseDirection(String value) {
    return "asc".equalsIgnoreCase(value) ? Sort.Direction.ASC : Sort.Direction.DESC;
  }

  private String clean(String value) {
    return value == null || value.isBlank() ? null : value.trim();
  }
}
