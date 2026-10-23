package com.devtrack.project;

import com.devtrack.common.CurrentUser;
import com.devtrack.common.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/v1/projects")
public class ProjectController {
  private final ProjectService service;

  public ProjectController(ProjectService service) { this.service = service; }

  @GetMapping
  public PageResponse<ProjectResponse> list(
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
      @RequestParam(required = false) String search,
      @RequestParam(required = false) ProjectStatus status,
      @RequestParam(defaultValue = "createdAt") String sort,
      @RequestParam(defaultValue = "desc") String direction) {
    return service.list(CurrentUser.id(), page, size, search, status, sort, direction);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ProjectResponse create(@Valid @RequestBody ProjectInput input) {
    return service.create(CurrentUser.id(), input);
  }

  @GetMapping("/{id}")
  public ProjectResponse get(@PathVariable UUID id) { return service.get(CurrentUser.id(), id); }

  @PutMapping("/{id}")
  public ProjectResponse update(@PathVariable UUID id, @Valid @RequestBody ProjectInput input) {
    return service.update(CurrentUser.id(), id, input);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) { service.delete(CurrentUser.id(), id); }
}
