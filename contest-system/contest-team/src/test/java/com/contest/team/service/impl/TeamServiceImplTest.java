package com.contest.team.service.impl;

import com.contest.common.constant.CommonConstants;
import com.contest.common.exception.BusinessException;
import com.contest.team.entity.TeamDO;
import com.contest.team.entity.TeamMemberDO;
import com.contest.team.service.TeamService;
import com.contest.team.test.TestApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = TestApplication.class)
@ActiveProfiles("test")
@Transactional
class TeamServiceImplTest {

    @Autowired
    private TeamService teamService;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        jdbc.execute("INSERT INTO `user` (id, username, password, name, role, status) VALUES (1, 'leader', 'pw', '队长', 0, 0)");
        jdbc.execute("INSERT INTO `user` (id, username, password, name, role, status) VALUES (2, 'member', 'pw', '成员A', 0, 0)");
        jdbc.execute("INSERT INTO `user` (id, username, password, name, role, status) VALUES (3, 'applicant', 'pw', '申请人', 0, 0)");
        jdbc.execute("INSERT INTO `user` (id, username, password, name, role, status) VALUES (4, 'other', 'pw', '其他', 0, 0)");
        jdbc.execute("INSERT INTO contest (id, name, contest_type, team_min_size, team_max_size, max_participants, status, register_start_time, register_end_time, contest_time) VALUES (1, '测试竞赛', 1, 2, 5, 100, 1, '2026-01-01 00:00:00', '2026-12-31 23:59:59', '2027-01-01 00:00:00')");
        jdbc.execute("INSERT INTO team (id, leader_id, team_name, team_no, status, member_count, invite_code, invite_code_expire) VALUES (1, 1, '测试团队', 'T00001', 0, 1, 'ABC123', '2026-12-31 23:59:59')");
        jdbc.execute("INSERT INTO team_member (team_id, user_id, role, status, apply_time) VALUES (1, 1, 1, 1, NOW())");
    }

    // ==================== createTeam ====================

    @Test
    void createTeam_shouldCreateTeamAndLeader() {
        TeamDO result = teamService.createTeam(2L, "新团队", null);
        assertNotNull(result);
        assertEquals(2L, result.getLeaderId());
        assertEquals("新团队", result.getTeamName());
        assertEquals(CommonConstants.TEAM_FORMING, result.getStatus());
        assertEquals(1, result.getMemberCount());
        assertTrue(result.getTeamNo().startsWith("T"));
    }

    // ==================== generateInviteCode ====================

    @Test
    void generateInviteCode_shouldThrowWhenTeamNotFound() {
        assertThrows(BusinessException.class, () -> teamService.generateInviteCode(999L, 1L));
    }

    @Test
    void generateInviteCode_shouldThrowWhenNotLeader() {
        assertThrows(BusinessException.class, () -> teamService.generateInviteCode(1L, 2L));
    }

    @Test
    void generateInviteCode_shouldSucceed() {
        String code = teamService.generateInviteCode(1L, 1L);
        assertNotNull(code);
        assertEquals(6, code.length());
    }

    // ==================== joinByInviteCode ====================

    @Test
    void joinByInviteCode_shouldThrowWhenInvalidCode() {
        assertThrows(BusinessException.class, () -> teamService.joinByInviteCode(3L, "XXXXXX"));
    }

    @Test
    void joinByInviteCode_shouldThrowWhenExpired() {
        jdbc.execute("UPDATE team SET invite_code_expire = '2020-01-01 00:00:00' WHERE id = 1");
        assertThrows(BusinessException.class, () -> teamService.joinByInviteCode(3L, "ABC123"));
    }

    @Test
    void joinByInviteCode_shouldThrowWhenNotForming() {
        jdbc.execute("UPDATE team SET status = 2 WHERE id = 1");
        assertThrows(BusinessException.class, () -> teamService.joinByInviteCode(3L, "ABC123"));
    }

    @Test
    void joinByInviteCode_shouldThrowWhenAlreadyApprovedMember() {
        jdbc.execute("INSERT INTO team_member (team_id, user_id, role, status, apply_time) VALUES (1, 3, 0, 1, NOW())");
        assertThrows(BusinessException.class, () -> teamService.joinByInviteCode(3L, "ABC123"));
    }

    @Test
    void joinByInviteCode_shouldSucceed() {
        TeamDO result = teamService.joinByInviteCode(3L, "ABC123");
        assertNotNull(result);
        Long memberId = jdbc.queryForObject("SELECT id FROM team_member WHERE team_id = 1 AND user_id = 3", Long.class);
        assertNotNull(memberId);
    }

    // ==================== approveMember ====================

    @Test
    void approveMember_shouldThrowWhenNotLeader() {
        assertThrows(BusinessException.class, () -> teamService.approveMember(1L, 2L, 1L));
    }

    @Test
    void approveMember_shouldSucceed() {
        jdbc.execute("INSERT INTO team_member (team_id, user_id, role, status, apply_time) VALUES (1, 3, 0, 0, NOW())");
        Long memberId = jdbc.queryForObject("SELECT id FROM team_member WHERE team_id = 1 AND user_id = 3", Long.class);
        teamService.approveMember(1L, 1L, memberId);
        Integer status = jdbc.queryForObject("SELECT status FROM team_member WHERE id = ?", Integer.class, memberId);
        assertEquals(CommonConstants.MEMBER_APPROVED, status.intValue());
    }

    // ==================== rejectMember ====================

    @Test
    void rejectMember_shouldDecrementCountWhenWasApproved() {
        // Add a member who is APPROVED
        jdbc.execute("INSERT INTO team_member (team_id, user_id, role, status, apply_time) VALUES (1, 3, 0, 1, NOW())");
        jdbc.execute("UPDATE team SET member_count = 2 WHERE id = 1");
        Long memberId = jdbc.queryForObject("SELECT id FROM team_member WHERE team_id = 1 AND user_id = 3", Long.class);
        teamService.rejectMember(1L, 1L, memberId);
        Integer status = jdbc.queryForObject("SELECT status FROM team_member WHERE id = ?", Integer.class, memberId);
        assertEquals(CommonConstants.MEMBER_REJECTED, status.intValue());
        Integer count = jdbc.queryForObject("SELECT member_count FROM team WHERE id = 1", Integer.class);
        assertEquals(1, count.intValue());
    }

    @Test
    void rejectMember_shouldNotDecrementWhenWasPending() {
        jdbc.execute("INSERT INTO team_member (team_id, user_id, role, status, apply_time) VALUES (1, 3, 0, 0, NOW())");
        Long memberId = jdbc.queryForObject("SELECT id FROM team_member WHERE team_id = 1 AND user_id = 3", Long.class);
        teamService.rejectMember(1L, 1L, memberId);
        Integer count = jdbc.queryForObject("SELECT member_count FROM team WHERE id = 1", Integer.class);
        assertEquals(1, count.intValue());
    }

    // ==================== removeMember ====================

    @Test
    void removeMember_shouldThrowWhenNotLeader() {
        assertThrows(BusinessException.class, () -> teamService.removeMember(1L, 2L, 1L));
    }

    @Test
    void removeMember_shouldThrowWhenMemberNotFound() {
        assertThrows(BusinessException.class, () -> teamService.removeMember(1L, 1L, 999L));
    }

    @Test
    void removeMember_shouldThrowWhenRemovingLeader() {
        Long leaderMemberId = jdbc.queryForObject("SELECT id FROM team_member WHERE team_id = 1 AND user_id = 1", Long.class);
        assertThrows(BusinessException.class, () -> teamService.removeMember(1L, 1L, leaderMemberId));
    }

    @Test
    void removeMember_shouldSucceed() {
        jdbc.execute("INSERT INTO team_member (team_id, user_id, role, status, apply_time) VALUES (1, 3, 0, 1, NOW())");
        jdbc.execute("UPDATE team SET member_count = 2 WHERE id = 1");
        Long memberId = jdbc.queryForObject("SELECT id FROM team_member WHERE team_id = 1 AND user_id = 3", Long.class);
        teamService.removeMember(1L, 1L, memberId);
        Integer status = jdbc.queryForObject("SELECT status FROM team_member WHERE id = ?", Integer.class, memberId);
        assertEquals(CommonConstants.MEMBER_REJECTED, status.intValue());
    }

    // ==================== dissolveTeam ====================

    @Test
    void dissolveTeam_shouldSucceed() {
        teamService.dissolveTeam(1L, 1L);
        TeamDO team = teamService.getById(1L);
        assertNull(team);
    }

    // ==================== leaveTeam ====================

    @Test
    void leaveTeam_shouldThrowWhenNotMember() {
        assertThrows(BusinessException.class, () -> teamService.leaveTeam(1L, 4L));
    }

    @Test
    void leaveTeam_shouldThrowWhenLeader() {
        assertThrows(BusinessException.class, () -> teamService.leaveTeam(1L, 1L));
    }

    @Test
    void leaveTeam_shouldSucceed() {
        jdbc.execute("INSERT INTO team_member (team_id, user_id, role, status, apply_time) VALUES (1, 3, 0, 1, NOW())");
        jdbc.execute("UPDATE team SET member_count = 2 WHERE id = 1");
        teamService.leaveTeam(1L, 3L);
        Integer status = jdbc.queryForObject("SELECT status FROM team_member WHERE team_id = 1 AND user_id = 3", Integer.class);
        assertEquals(CommonConstants.MEMBER_REJECTED, status.intValue());
        Integer count = jdbc.queryForObject("SELECT member_count FROM team WHERE id = 1", Integer.class);
        assertEquals(1, count.intValue());
    }

    // ==================== submitForReview ====================

    @Test
    void submitForReview_shouldThrowWhenNotLeader() {
        assertThrows(BusinessException.class, () -> teamService.submitForReview(1L, 2L));
    }

    @Test
    void submitForReview_shouldThrowWhenTeamNotFound() {
        assertThrows(BusinessException.class, () -> teamService.submitForReview(999L, 1L));
    }

    // ==================== getTeamsByLeader ====================

    @Test
    void getTeamsByLeader_shouldReturnTeams() {
        List<TeamDO> result = teamService.getTeamsByLeader(1L);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getLeaderId());
    }

    @Test
    void getTeamsByLeader_shouldReturnEmptyWhenNone() {
        List<TeamDO> result = teamService.getTeamsByLeader(4L);
        assertTrue(result.isEmpty());
    }

    // ==================== getTeamsByTeacher ====================

    @Test
    void getTeamsByTeacher_shouldReturnEmptyWhenNone() {
        List<TeamDO> result = teamService.getTeamsByTeacher(1L);
        assertTrue(result.isEmpty());
    }

    // ==================== team CRUD ====================

    @Test
    void getById_shouldReturnTeam() {
        TeamDO result = teamService.getById(1L);
        assertNotNull(result);
        assertEquals("测试团队", result.getTeamName());
    }

    @Test
    void getById_shouldReturnNullWhenNotFound() {
        TeamDO result = teamService.getById(999L);
        assertNull(result);
    }

    // ==================== adminApproveTeam ====================

    @Test
    void adminApproveTeam_shouldThrowWhenNotSubmitted() {
        assertThrows(BusinessException.class, () -> teamService.adminApproveTeam(1L));
    }

    @Test
    void adminApproveTeam_shouldThrowWhenTeamNotFound() {
        assertThrows(BusinessException.class, () -> teamService.adminApproveTeam(999L));
    }

    // ==================== adminRejectTeam ====================

    @Test
    void adminRejectTeam_shouldThrowWhenReasonTooShort() {
        assertThrows(BusinessException.class, () -> teamService.adminRejectTeam(1L, "否"));
    }

    @Test
    void adminRejectTeam_shouldThrowWhenNullReason() {
        assertThrows(BusinessException.class, () -> teamService.adminRejectTeam(1L, null));
    }

    // ==================== listUserTeams ====================

    @Test
    void listUserTeams_shouldReturnTeams() {
        List<TeamDO> result = teamService.listUserTeams(1L);
        assertFalse(result.isEmpty());
    }

    @Test
    void listUserTeams_shouldReturnEmptyWhenNoMembership() {
        List<TeamDO> result = teamService.listUserTeams(4L);
        assertTrue(result.isEmpty());
    }

    // ==================== listMembers ====================

    @Test
    void listMembers_shouldReturnApprovedMembers() {
        jdbc.execute("INSERT INTO team_member (team_id, user_id, role, status, apply_time, handle_time) VALUES (1, 2, 0, 1, NOW(), NOW())");
        List<TeamMemberDO> result = teamService.listMembers(1L);
        assertEquals(2, result.size());
    }

    @Test
    void listMembers_shouldHandleNoMembers() {
        jdbc.execute("DELETE FROM team_member WHERE team_id = 1");
        jdbc.execute("INSERT INTO team_member (team_id, user_id, role, status) VALUES (1, 1, 1, 1)");
        List<TeamMemberDO> result = teamService.listMembers(1L);
        assertEquals(1, result.size());
    }

    // ==================== listPendingMembers ====================

    @Test
    void listPendingMembers_shouldReturnPendingMembers() {
        jdbc.execute("INSERT INTO team_member (team_id, user_id, role, status, apply_time) VALUES (1, 3, 0, 0, NOW())");
        List<TeamMemberDO> result = teamService.listPendingMembers(1L);
        assertEquals(1, result.size());
    }

    // ==================== pageTeams ====================

    @Test
    void pageTeams_shouldReturnPagedResult() {
        var result = teamService.pageTeams(null, 1, 10);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
    }

    @Test
    void pageTeams_shouldFilterByStatus() {
        var result = teamService.pageTeams(0, 1, 10);
        assertEquals(1, result.getTotal());
    }

    // ==================== setTeacher ====================

    @Test
    void setTeacher_shouldThrowWhenNotLeader() {
        assertThrows(BusinessException.class, () -> teamService.setTeacher(1L, 13L, 2L));
    }

    // ==================== invite code expiration ====================

    @Test
    void generateInviteCode_shouldUpdateExpiration() {
        teamService.generateInviteCode(1L, 1L);
        TeamDO team = teamService.getById(1L);
        assertNotNull(team.getInviteCode());
        assertNotNull(team.getInviteCodeExpire());
    }
}
