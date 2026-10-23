package com.devtrack.skill;

import com.devtrack.common.CurrentUser;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
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

@RestController
@RequestMapping("/v1/skills")
public class SkillController {
  private final SkillService service;

  public SkillController(SkillService service) { this.service = service; }

  @GetMapping
  public List<SkillResponse> list(
      @RequestParam(required = false) String category,
      @RequestParam(required = false) String search,
      @RequestParam(defaultValue = "name") String sort,
      @RequestParam(defaultValue = "asc") String direction) {
    return service.list(CurrentUser.id(), category, search, sort, direction);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public SkillResponse create(@Valid @RequestBody SkillInput input) {
    return service.create(CurrentUser.id(), input);
  }

  @PutMapping("/{id}")
  public SkillResponse update(@PathVariable UUID id, @Valid @RequestBody SkillInput input) {
    return service.update(CurrentUser.id(), id, input);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) { service.delete(CurrentUser.id(), id); }
}
