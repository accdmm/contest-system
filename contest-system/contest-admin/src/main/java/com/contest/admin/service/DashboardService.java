package com.contest.admin.service;

import java.util.List;
import java.util.Map;

public interface DashboardService {

    Map<String, Long> getStatistics();

    List<Map<String, Object>> getContestCategoryDistribution();

    List<Map<String, Object>> getContestLevelDistribution();

    List<Map<String, Object>> getRegistrationTrend(int days);

    List<Map<String, Object>> getRegistrationStatusDistribution();

    List<Map<String, Object>> getUserGrowth(int days);

    List<Map<String, Object>> getTopContests(int limit);
}
