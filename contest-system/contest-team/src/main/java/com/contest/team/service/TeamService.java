package com.contest.team.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.contest.team.entity.Team;
import com.contest.team.entity.TeamMember;

import java.util.List;

/** 团队服务接口 */
public interface TeamService extends IService<Team> {

    /** 创建团队
     * @param userId 队长用户ID
     * @param teamName 团队名称
     * @param teacherId 指导教师ID（可为空）
     * @return 创建的团队对象 */
    Team createTeam(Long userId, String teamName, Long teacherId);

    /** 生成团队邀请码
     * @param teamId 团队ID
     * @param userId 操作人用户ID（需为队长）
     * @return 6位邀请码 */
    String generateInviteCode(Long teamId, Long userId);

    /** 通过邀请码加入团队
     * @param userId 加入者用户ID
     * @param inviteCode 邀请码
     * @return 团队对象 */
    Team joinByInviteCode(Long userId, String inviteCode);

    /** 队长批准成员加入
     * @param teamId 团队ID
     * @param userId 队长用户ID
     * @param memberId 成员记录ID */
    void approveMember(Long teamId, Long userId, Long memberId);

    /** 队长拒绝成员加入
     * @param teamId 团队ID
     * @param userId 队长用户ID
     * @param memberId 成员记录ID */
    void rejectMember(Long teamId, Long userId, Long memberId);

    /** 队长移除成员
     * @param teamId 团队ID
     * @param userId 队长用户ID
     * @param memberId 成员记录ID */
    void removeMember(Long teamId, Long userId, Long memberId);

    /** 队长解散团队
     * @param teamId 团队ID
     * @param userId 队长用户ID */
    void dissolveTeam(Long teamId, Long userId);

    /** 成员退出团队
     * @param teamId 团队ID
     * @param userId 成员用户ID */
    void leaveTeam(Long teamId, Long userId);

    /** 队长提交团队审核
     * @param teamId 团队ID
     * @param userId 队长用户ID */
    void submitForReview(Long teamId, Long userId);

    /** 获取团队成员列表
     * @param teamId 团队ID
     * @return 已通过成员列表 */
    List<TeamMember> listMembers(Long teamId);

    /** 获取待审批成员列表
     * @param teamId 团队ID
     * @return 待审批成员列表 */
    List<TeamMember> listPendingMembers(Long teamId);

    /** 获取用户创建的团队
     * @param userId 用户ID
     * @return 团队列表 */
    List<Team> getTeamsByLeader(Long userId);

    /** 分页查询团队（管理员）
     * @param status 团队状态筛选
     * @param page 页码
     * @param size 每页条数
     * @return 分页结果 */
    IPage<Team> pageTeams(Integer status, Integer page, Integer size);

    /** 管理员通过团队审核
     * @param teamId 团队ID */
    void adminApproveTeam(Long teamId);

    /** 管理员驳回团队审核
     * @param teamId 团队ID
     * @param reason 驳回原因 */
    void adminRejectTeam(Long teamId, String reason);

    /** 获取用户参与的团队列表
     * @param userId 用户ID
     * @return 团队列表 */
    List<Team> listUserTeams(Long userId);

    /** 设置团队指导教师
     * @param teamId 团队ID
     * @param teacherId 教师用户ID
     * @param userId 队长用户ID */
    void setTeacher(Long teamId, Long teacherId, Long userId);

    /** 获取教师指导的团队列表
     * @param teacherId 教师用户ID
     * @return 团队列表 */
    List<Team> getTeamsByTeacher(Long teacherId);
}
