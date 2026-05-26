package com.contest.ai.tool;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.contest.competition.entity.Contest;
import com.contest.competition.service.ContestService;
import com.contest.register.entity.Registration;
import com.contest.register.service.RegistrationService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ChatTools {

    private static final ThreadLocal<Long> CURRENT_USER_ID = new ThreadLocal<>();

    private final ContestService contestService;
    private final RegistrationService registrationService;

    public ChatTools(ContestService contestService, RegistrationService registrationService) {
        this.contestService = contestService;
        this.registrationService = registrationService;
    }

    public static void setCurrentUserId(Long userId) { CURRENT_USER_ID.set(userId); }
    public static void clearUserId() { CURRENT_USER_ID.remove(); }

    @Tool(description = "查询正在报名的竞赛列表，支持按分类和关键字过滤")
    public String queryContests(String keyword, String category, int page, int size) {
        try {
            IPage<Contest> result = contestService.pageContests(page, size, keyword, category, 1);
            List<Contest> records = result.getRecords();
            if (records.isEmpty()) {
                return "当前没有找到符合条件的竞赛";
            }
            return records.stream()
                .map(c -> String.format("【%s】%s | 分类: %s | 级别: %s | 报名截止: %s | 已报名: %d人",
                    c.getId(), c.getName(), c.getCategory(), c.getLevel(),
                    c.getRegisterEndTime(), c.getCurrentCount()))
                .collect(Collectors.joining("\n"));
        } catch (Exception e) {
            return "查询竞赛失败: " + e.getMessage();
        }
    }

    @Tool(description = "查询指定竞赛的详细信息")
    public String getContestDetail(Long contestId) {
        try {
            Contest c = contestService.getById(contestId);
            if (c == null) {
                return "竞赛不存在";
            }
            return String.format(
                "【%s】\n分类: %s\n级别: %s\n主办方: %s\n竞赛时间: %s\n报名时间: %s ~ %s\n地点: %s\n类型: %s\n已报名: %d人\n简介: %s",
                c.getName(), c.getCategory(), c.getLevel(), c.getOrganizer(),
                c.getContestTime(), c.getRegisterStartTime(), c.getRegisterEndTime(),
                c.getLocation(),
                c.getContestType() == 0 ? "个人" : c.getContestType() == 1 ? "团队" : "个人/团队",
                c.getCurrentCount(),
                c.getDescription() != null && c.getDescription().length() > 200
                    ? c.getDescription().substring(0, 200) + "..."
                    : c.getDescription()
            );
        } catch (Exception e) {
            return "查询竞赛详情失败: " + e.getMessage();
        }
    }

    @Tool(description = "查询当前用户的竞赛报名状态")
    public String queryMyRegistrations() {
        Long userId = CURRENT_USER_ID.get();
        if (userId == null) {
            return "无法获取当前用户信息";
        }
        try {
            IPage<Registration> result = registrationService.pageByUser(userId, 1, 20);
            List<Registration> records = result.getRecords();
            if (records.isEmpty()) {
                return "你还没有报名任何竞赛";
            }
            return records.stream()
                .map(r -> {
                    String statusStr = switch (r.getStatus()) {
                        case 0 -> "待审核";
                        case 1 -> "已通过";
                        case 2 -> "已拒绝";
                        case 3 -> "已取消";
                        default -> "未知";
                    };
                    return String.format("竞赛ID: %d | 类型: %s | 状态: %s%s",
                        r.getContestId(),
                        r.getRegType() == 0 ? "个人" : "团队",
                        statusStr,
                        r.getReviewReason() != null ? " | 原因: " + r.getReviewReason() : "");
                })
                .collect(Collectors.joining("\n"));
        } catch (Exception e) {
            return "查询报名状态失败: " + e.getMessage();
        }
    }
}
