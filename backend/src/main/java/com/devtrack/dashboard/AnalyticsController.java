package com.devtrack.dashboard;

import com.devtrack.activity.ActivityService;
import com.devtrack.activity.ActivityService.ActivityDay;
import com.devtrack.common.CurrentUser;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/v1/analytics")
public class AnalyticsController {
  private final ActivityService activity;

  public AnalyticsController(ActivityService activity) { this.activity = activity; }

  @GetMapping("/activity")
  public List<ActivityDay> activity(
      @RequestParam(defaultValue = "30") @Min(7) @Max(365) int days) {
    return activity.dailyCounts(CurrentUser.id(), days);
  }
}
