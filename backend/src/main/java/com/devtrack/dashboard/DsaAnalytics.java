package com.devtrack.dashboard;

import java.util.List;

public record DsaAnalytics(
    List<DashboardSummary.DifficultyCount> difficultyDistribution,
    List<DashboardSummary.TopicProgress> topicProgress
) {}
