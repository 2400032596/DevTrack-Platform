package com.devtrack.dashboard;

import com.devtrack.activity.ActivityResponse;
import com.devtrack.activity.ActivityService.ActivityDay;
import com.devtrack.problem.Difficulty;
import com.devtrack.project.ProjectStatus;
import java.util.List;
import java.util.UUID;

public record DashboardSummary(
    ProblemTotals problemTotals,
    ProjectTotals projectTotals,
    SkillTotals skillTotals,
    List<ActivityDay> weeklyActivity,
    List<DifficultyCount> difficultyDistribution,
    List<TopicProgress> topicProgress,
    List<StatusCount> projectsByStatus,
    List<ActivityResponse> recentActivity,
    List<SkillGoal> goals
) {
  public record ProblemTotals(long total, long solved, long inProgress, long todo) {}
  public record ProjectTotals(long total, long planned, long inProgress, long completed) {}
  public record SkillTotals(long total, double averageProficiency, long targetMet) {}
  public record DifficultyCount(Difficulty difficulty, long total, long solved) {}
  public record TopicProgress(String topic, long total, long solved) {}
  public record StatusCount(ProjectStatus status, long total) {}
  public record SkillGoal(
      UUID id, String name, String category, int proficiencyLevel, int targetLevel, int progressPercent) {}
}
