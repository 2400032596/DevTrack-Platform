package com.devtrack.dashboard;

import com.devtrack.common.CurrentUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
public class DashboardController {
  private final DashboardService dashboard;

  public DashboardController(DashboardService dashboard) { this.dashboard = dashboard; }

  @GetMapping("/dashboard/summary")
  public DashboardSummary summary() { return dashboard.summary(CurrentUser.id()); }

  @GetMapping("/analytics/dsa")
  public DsaAnalytics dsa() { return dashboard.dsa(CurrentUser.id()); }
}
