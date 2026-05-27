package com.contest.ai.tool;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.contest.competition.entity.Contest;
import com.contest.competition.service.ContestService;
import com.contest.common.constant.CommonConstants;
import com.contest.register.entity.Registration;
import com.contest.register.service.RegistrationService;
import com.contest.team.entity.Team;
import com.contest.team.service.TeamService;
import com.contest.user.entity.User;
import com.contest.user.service.UserService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ChatTools {

    private static final ThreadLocal<Long> CURRENT_USER_ID = new ThreadLocal<>();

    private final ContestService contestService;
    private final RegistrationService registrationService;
    private final UserService userService;
    private final TeamService teamService;

    public ChatTools(ContestService contestService, RegistrationService registrationService,
                     UserService userService, TeamService teamService) {
        this.contestService = contestService;
        this.registrationService = registrationService;
        this.userService = userService;
        this.teamService = teamService;
    }

    public static void setCurrentUserId(Long userId) { CURRENT_USER_ID.set(userId); }
    public static void clearUserId() { CURRENT_USER_ID.remove(); }

    @Tool(description = "浏览正在报名的竞赛列表，支持按分类和关键字过滤。如需搜索特定竞赛请使用 searchContestDetail")
    public String queryContests(String keyword, String category, Integer page, Integer size) {
        try {
            int p = page != null && page > 0 ? page : 1;
            int s = size != null && size > 0 ? size : 10;
            IPage<Contest> result = contestService.pageContests(p, s, keyword, category, 1);
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
                    String contestName = "";
                    try {
                        Contest c = contestService.getById(r.getContestId());
                        if (c != null) contestName = c.getName();
                    } catch (Exception ignored) {}
                    return String.format("%s | 类型: %s | 状态: %s%s",
                        contestName,
                        r.getRegType() == 0 ? "个人" : "团队",
                        statusStr,
                        r.getReviewReason() != null ? " | 原因: " + r.getReviewReason() : "");
                })
                .collect(Collectors.joining("\n"));
        } catch (Exception e) {
            return "查询报名状态失败: " + e.getMessage();
        }
    }

    @Tool(description = "获取当前登录用户的个人信息")
    public String getCurrentUserInfo() {
        Long userId = CURRENT_USER_ID.get();
        if (userId == null) {
            return "无法获取当前用户信息，请先登录";
        }
        try {
            User user = userService.getById(userId);
            if (user == null) {
                return "用户不存在";
            }
            return String.format(
                "姓名: %s\n学号: %s\n学院: %s\n专业: %s\n班级: %s\n邮箱: %s\n电话: %s",
                user.getName(), user.getUsername(),
                user.getCollege() != null ? user.getCollege() : "未设置",
                user.getMajor() != null ? user.getMajor() : "未设置",
                user.getClassName() != null ? user.getClassName() : "未设置",
                user.getEmail() != null ? user.getEmail() : "未设置",
                user.getPhone() != null ? user.getPhone() : "未设置"
            );
        } catch (Exception e) {
            return "获取用户信息失败: " + e.getMessage();
        }
    }

    @Tool(description = "【推荐】按竞赛名称精确搜索某个竞赛并查看详细信息，不需要事先知道竞赛ID。查找特定竞赛用此工具而非queryContests")
    public String searchContestDetail(String contestName) {
        try {
            IPage<Contest> result = contestService.pageContests(1, 5, contestName, null, 1);
            List<Contest> records = result.getRecords();
            if (records.isEmpty()) {
                return "未找到名称为「" + contestName + "」的竞赛";
            }
            Contest c = records.get(0);
            return String.format(
                "【%s】\n竞赛ID: %s\n分类: %s\n级别: %s\n主办方: %s\n竞赛时间: %s\n报名时间: %s ~ %s\n地点: %s\n类型: %s\n已报名: %d人\n简介: %s",
                c.getName(), c.getId(), c.getCategory(), c.getLevel(), c.getOrganizer(),
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

    @Tool(description = "查询当前用户创建或加入的所有团队及状态")
    public String queryMyTeams() {
        Long userId = CURRENT_USER_ID.get();
        if (userId == null) {
            return "无法获取当前用户信息";
        }
        try {
            List<Team> teams = teamService.listUserTeams(userId);
            if (teams.isEmpty()) {
                return "你还没有创建或加入任何团队";
            }
            return teams.stream()
                .map(t -> {
                    String statusStr = switch (t.getStatus()) {
                        case 0 -> "组建中";
                        case 1 -> "已提交审核";
                        case 2 -> "已通过";
                        case 3 -> "已驳回";
                        default -> "未知";
                    };
                    String contestName = "";
                    try {
                        Registration reg = registrationService.lambdaQuery()
                            .eq(com.contest.register.entity.Registration::getTeamId, t.getId())
                            .last("LIMIT 1").one();
                        if (reg != null) {
                            Contest c = contestService.getById(reg.getContestId());
                            if (c != null) contestName = c.getName();
                        }
                    } catch (Exception ignored) {}
                    return String.format("团队: %s | 竞赛: %s | 状态: %s | 人数: %d | 邀请码: %s",
                        t.getTeamName(), contestName, statusStr, t.getMemberCount(),
                        t.getInviteCode() != null ? t.getInviteCode() : "无");
                })
                .collect(Collectors.joining("\n"));
        } catch (Exception e) {
            return "查询团队信息失败: " + e.getMessage();
        }
    }

    @Tool(description = "报名参加个人赛（竞赛类型为个人赛或个人/团队赛均可），需提供竞赛名称，将自动搜索并报名。remark为可选备注")
    public String registerForContest(String contestName, String remark) {
        Long userId = CURRENT_USER_ID.get();
        if (userId == null) {
            return "无法获取当前用户信息，请先登录";
        }
        try {
            IPage<Contest> result = contestService.pageContests(1, 10, contestName, null, 1);
            List<Contest> records = result.getRecords();
            if (records.isEmpty()) {
                return "未找到名称为「" + contestName + "」的竞赛，请确认名称是否正确";
            }
            Contest contest = records.get(0);
            if (contest.getContestType() == CommonConstants.CONTEST_TEAM) {
                return "「" + contest.getName() + "」为团队赛，不支持个人报名。请先调用 createTeamForContest 创建团队，然后由队长提交审核。";
            }
            try {
                registrationService.registerPersonal(userId, contest.getId(), remark);
                return "报名成功！您已成功报名「" + contest.getName() + "」，当前状态为待审核，请耐心等待管理员审核。";
            } catch (Exception e) {
                String msg = e.getMessage();
                if (msg != null && msg.contains("您已报名该竞赛")) {
                    return "您已报名过「" + contest.getName() + "」，无需重复报名";
                }
                return "报名失败: " + (msg != null ? msg : "未知错误");
            }
        } catch (Exception e) {
            return "报名失败: " + e.getMessage();
        }
    }

    @Tool(description = "创建团队参加团队赛（竞赛类型为团队赛或个人/团队赛均可），创建者自动成为队长。对仅个人赛的竞赛请调用registerForContest。注意：必须先询问用户想要的团队名称再调用此工具")
    public String createTeamForContest(String contestName, @ToolParam(required = false) String teamName) {
        Long userId = CURRENT_USER_ID.get();
        if (userId == null) {
            return "无法获取当前用户信息，请先登录";
        }
        if (teamName == null || teamName.isBlank()) {
            return "请提供团队名称，例如你想创建的团队叫什么名字？";
        }
        try {
            IPage<Contest> result = contestService.pageContests(1, 10, contestName, null, 1);
            List<Contest> records = result.getRecords();
            if (records.isEmpty()) {
                return "未找到名称为「" + contestName + "」的竞赛";
            }
            Contest contest = records.get(0);
            if (contest.getContestType() == CommonConstants.CONTEST_PERSONAL) {
                return "「" + contest.getName() + "」为个人赛，不需要创建团队，请直接使用 registerForContest 报名";
            }
            try {
                com.contest.team.entity.Team team = teamService.createTeam(userId, teamName);
                return "团队创建成功！\n团队名称: " + team.getTeamName()
                    + "\n团队编号: " + team.getTeamNo()
                    + "\n竞赛: " + contest.getName()
                    + "\n邀请码: " + team.getInviteCode()
                    + "\n\n将邀请码分享给队友，他们可以加入你的团队。组队完成后，队长可以提交审核。";
            } catch (Exception e) {
                String msg = e.getMessage();
                if (msg != null) {
                    if (msg.contains("你已在同一竞赛的团队中")) {
                        return "你已经在该竞赛中创建或加入了团队，无需重复创建。如需查看团队信息，请使用 queryMyTeams 工具。";
                    }
                    if (msg.contains("该竞赛仅限个人报名，无法创建团队")) {
                        return "「" + contest.getName() + "」为个人赛，不需要创建团队，请直接使用 registerForContest 报名";
                    }
                }
                return "创建团队失败: " + (msg != null ? msg : "未知错误");
            }
        } catch (Exception e) {
            return "创建团队失败: " + e.getMessage();
        }
    }
}
