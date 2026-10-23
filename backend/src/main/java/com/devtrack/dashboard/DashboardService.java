package com.devtrack.dashboard;

import com.devtrack.activity.ActivityService;
import com.devtrack.problem.Difficulty;
import com.devtrack.problem.ProblemRepository;
import com.devtrack.problem.ProblemStatus;
import com.devtrack.project.ProjectRepository;
import com.devtrack.project.ProjectStatus;
import com.devtrack.skill.Skill;
import com.devtrack.skill.SkillRepository;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {
  private final ProblemRepository problems;
  private final ProjectRepository projects;
  private final SkillRepository skills;
  private final ActivityService activity;

  public DashboardService(
      ProblemRepository problems,
      ProjectRepository projects,
      SkillRepository skills,
      ActivityService activity) {
    this.problems = problems;
    this.projects = projects;
    this.skills = skills;
    this.activity = activity;
  }

  @Transactional(readOnly = true)
  public DashboardSummary summary(UUID userId) {
    long problemTotal = problems.countByUserId(userId);
    long solved = problems.countByUserIdAndStatus(userId, ProblemStatus.SOLVED);
    long inProgress = problems.countByUserIdAndStatus(userId, ProblemStatus.IN_PROGRESS);
    long todo = problems.countByUserIdAndStatus(userId, ProblemStatus.TODO);

    long projectTotal = projects.countByUserId(userId);
    long planned = projects.countByUserIdAndStatus(userId, ProjectStatus.PLANNED);
    long active = projects.countByUserIdAndStatus(userId, ProjectStatus.IN_PROGRESS);
    long completed = projects.countByUserIdAndStatus(userId, ProjectStatus.COMPLETED);

    List<Skill> userSkills = skills.findAllByUserId(userId);
    double average = userSkills.stream().mapToInt(Skill::getProficiencyLevel).average().orElse(0);
    long targetMet = userSkills.stream()
        .filter(skill -> skill.getProficiencyLevel() >= skill.getTargetLevel()).count();
    List<DashboardSummary.SkillGoal> goals = userSkills.stream()
        .filter(skill -> skill.getProficiencyLevel() < skill.getTargetLevel())
        .sorted((a, b) -> Integer.compare(
            b.getTargetLevel() - b.getProficiencyLevel(),
            a.getTargetLevel() - a.getProficiencyLevel()))
        .limit(5)
        .map(skill -> new DashboardSummary.SkillGoal(
            skill.getId(), skill.getName(), skill.getCategory(),
            skill.getProficiencyLevel(), skill.getTargetLevel(),
            Math.min(100, skill.getProficiencyLevel() * 100 / skill.getTargetLevel())))
        .toList();

    return new DashboardSummary(
        new DashboardSummary.ProblemTotals(problemTotal, solved, inProgress, todo),
        new DashboardSummary.ProjectTotals(projectTotal, planned, active, completed),
        new DashboardSummary.SkillTotals(userSkills.size(), average, targetMet),
        activity.dailyCounts(userId, 7),
        difficultyDistribution(userId),
        topicProgress(userId),
        Arrays.stream(ProjectStatus.values())
            .map(status -> new DashboardSummary.StatusCount(
                status, projects.countByUserIdAndStatus(userId, status)))
            .toList(),
        activity.recent(userId),
        goals);
  }

  @Transactional(readOnly = true)
  public DsaAnalytics dsa(UUID userId) {
    return new DsaAnalytics(difficultyDistribution(userId), topicProgress(userId));
  }

  private List<DashboardSummary.DifficultyCount> difficultyDistribution(UUID userId) {
    return Arrays.stream(Difficulty.values())
        .map(difficulty -> new DashboardSummary.DifficultyCount(
            difficulty,
            problems.countByUserIdAndDifficulty(userId, difficulty),
            problems.countByUserIdAndDifficultyAndStatus(userId, difficulty, ProblemStatus.SOLVED)))
        .toList();
  }

  private List<DashboardSummary.TopicProgress> topicProgress(UUID userId) {
    return problems.topicProgress(userId).stream()
        .map(row -> new DashboardSummary.TopicProgress(row.getTopic(), row.getTotal(), row.getSolved()))
        .toList();
  }
}
