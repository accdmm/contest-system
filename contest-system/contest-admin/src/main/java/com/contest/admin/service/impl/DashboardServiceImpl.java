package com.contest.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.contest.admin.service.DashboardService;
import com.contest.competition.entity.Contest;
import com.contest.competition.mapper.ContestMapper;
import com.contest.competition.service.ContestService;
import com.contest.register.entity.Registration;
import com.contest.register.mapper.RegistrationMapper;
import com.contest.register.service.RegistrationService;
import com.contest.team.service.TeamService;
import com.contest.user.entity.User;
import com.contest.user.mapper.UserMapper;
import com.contest.user.service.UserService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final UserService userService;
    private final ContestService contestService;
    private final RegistrationService registrationService;
    private final TeamService teamService;
    private final UserMapper userMapper;
    private final ContestMapper contestMapper;
    private final RegistrationMapper registrationMapper;

    public DashboardServiceImpl(UserService userService, ContestService contestService,
                            RegistrationService registrationService, TeamService teamService,
                            UserMapper userMapper, ContestMapper contestMapper,
                            RegistrationMapper registrationMapper) {
        this.userService = userService;
        this.contestService = contestService;
        this.registrationService = registrationService;
        this.teamService = teamService;
        this.userMapper = userMapper;
        this.contestMapper = contestMapper;
        this.registrationMapper = registrationMapper;
    }

    /** 概览统计：总用户数、总竞赛数、总报名数、总团队数 */
    public Map<String, Long> getStatistics() {
        Map<String, Long> stats = new LinkedHashMap<>();
        stats.put("totalUsers", userService.count());
        stats.put("totalContests", contestService.count());
        stats.put("totalRegistrations", registrationService.count());
        stats.put("totalTeams", teamService.count());
        return stats;
    }

    /** 竞赛类别分布（饼图） */
    public List<Map<String, Object>> getContestCategoryDistribution() {
        QueryWrapper<Contest> wrapper = new QueryWrapper<>();
        wrapper.select("category, COUNT(*) as count")
               .isNotNull("category")
               .groupBy("category");
        return contestMapper.selectMaps(wrapper);
    }

    /** 竞赛级别分布（柱状图） */
    public List<Map<String, Object>> getContestLevelDistribution() {
        QueryWrapper<Contest> wrapper = new QueryWrapper<>();
        wrapper.select("level, COUNT(*) as count")
               .isNotNull("level")
               .groupBy("level");
        return contestMapper.selectMaps(wrapper);
    }

    /** 报名趋势（折线图） */
    public List<Map<String, Object>> getRegistrationTrend(int days) {
        QueryWrapper<Registration> wrapper = new QueryWrapper<>();
        wrapper.select("DATE(create_time) as date, COUNT(*) as count")
               .ge("create_time", LocalDateTime.now().minusDays(days))
               .groupBy("DATE(create_time)")
               .orderByAsc("DATE(create_time)");
        return registrationMapper.selectMaps(wrapper);
    }

    /** 报名状态分布（环图） */
    public List<Map<String, Object>> getRegistrationStatusDistribution() {
        QueryWrapper<Registration> wrapper = new QueryWrapper<>();
        wrapper.select("status, COUNT(*) as count")
               .groupBy("status");
        return registrationMapper.selectMaps(wrapper);
    }

    /** 用户增长趋势（折线图） */
    public List<Map<String, Object>> getUserGrowth(int days) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.select("DATE(create_time) as date, COUNT(*) as count")
               .ge("create_time", LocalDateTime.now().minusDays(days))
               .groupBy("DATE(create_time)")
               .orderByAsc("DATE(create_time)");
        return userMapper.selectMaps(wrapper);
    }

    /** 热门竞赛 Top N（横向柱状图） */
    public List<Map<String, Object>> getTopContests(int limit) {
        QueryWrapper<Contest> wrapper = new QueryWrapper<>();
        wrapper.select("id, name, current_count")
               .orderByDesc("current_count")
               .last("LIMIT " + limit);
        return contestMapper.selectMaps(wrapper);
    }
}
