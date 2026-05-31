package com.contest.team.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.contest.common.constant.CommonConstants;
import com.contest.common.exception.BusinessException;
import com.contest.competition.service.ContestService;
import com.contest.message.service.NotificationService;
import com.contest.register.entity.Registration;
import com.contest.register.service.AdminNotifyService;
import com.contest.register.service.RegistrationService;
import com.contest.team.entity.Team;
import com.contest.team.entity.TeamMember;
import com.contest.team.mapper.TeamMapper;
import com.contest.team.mapper.TeamMemberMapper;
import com.contest.user.entity.User;
import com.contest.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeamServiceImplTest {

    @Mock private TeamMapper teamMapper;
    @Mock private TeamMemberMapper teamMemberMapper;
    @Mock private ContestService contestService;
    @Mock private UserService userService;
    @Mock private RegistrationService registrationService;
    @Mock private NotificationService notificationService;
    @Mock private AdminNotifyService adminNotifyService;

    private TeamServiceImpl teamService;

    @BeforeEach
    void setUp() {
        teamService = new TeamServiceImpl(teamMemberMapper, contestService, userService, registrationService, notificationService, adminNotifyService);
        ReflectionTestUtils.setField(teamService, "baseMapper", teamMapper);
    }

    @Test
    void createTeam_shouldCreateTeamAndLeader() {
        when(teamMapper.insert(any(Team.class))).thenReturn(1);
        when(teamMemberMapper.insert(any(TeamMember.class))).thenReturn(1);

        Team result = teamService.createTeam(1L, "测试团队", null);

        assertNotNull(result);
        assertEquals(1L, result.getLeaderId());
        assertEquals("测试团队", result.getTeamName());
        assertEquals(CommonConstants.TEAM_FORMING, result.getStatus());
        assertEquals(1, result.getMemberCount());
        assertTrue(result.getTeamNo().startsWith("T"));
        verify(teamMapper).insert(any(Team.class));
        verify(teamMemberMapper).insert(any(TeamMember.class));
    }

    @Test
    void generateInviteCode_shouldThrowWhenTeamNotFound() {
        when(teamMapper.selectById(1L)).thenReturn(null);

        assertThrows(BusinessException.class, () -> teamService.generateInviteCode(1L, 1L));
    }

    @Test
    void generateInviteCode_shouldThrowWhenNotLeader() {
        Team team = new Team();
        team.setId(1L);
        team.setLeaderId(1L);
        when(teamMapper.selectById(1L)).thenReturn(team);

        assertThrows(BusinessException.class, () -> teamService.generateInviteCode(1L, 2L));
    }

    @Test
    void generateInviteCode_shouldSucceed() {
        Team team = new Team();
        team.setId(1L);
        team.setLeaderId(1L);
        when(teamMapper.selectById(1L)).thenReturn(team);
        when(teamMapper.updateById(any(Team.class))).thenReturn(1);

        String code = teamService.generateInviteCode(1L, 1L);

        assertNotNull(code);
        assertEquals(6, code.length());
        assertNotNull(team.getInviteCode());
        assertNotNull(team.getInviteCodeExpire());
    }

    @Test
    void joinByInviteCode_shouldThrowWhenInvalidCode() {
        when(teamMapper.selectOne(any(LambdaQueryWrapper.class), anyBoolean())).thenReturn(null);

        assertThrows(BusinessException.class, () -> teamService.joinByInviteCode(1L, "INVALID"));
    }

    @Test
    void joinByInviteCode_shouldThrowWhenExpired() {
        Team team = new Team();
        team.setId(1L);
        team.setInviteCode("ABC123");
        team.setInviteCodeExpire(LocalDateTime.now().minusDays(1));
        team.setStatus(CommonConstants.TEAM_FORMING);
        when(teamMapper.selectOne(any(LambdaQueryWrapper.class), anyBoolean())).thenReturn(team);

        assertThrows(BusinessException.class, () -> teamService.joinByInviteCode(2L, "ABC123"));
    }

    @Test
    void joinByInviteCode_shouldThrowWhenNotForming() {
        Team team = new Team();
        team.setId(1L);
        team.setInviteCode("ABC123");
        team.setInviteCodeExpire(LocalDateTime.now().plusDays(1));
        team.setStatus(CommonConstants.TEAM_SUBMITTED);
        when(teamMapper.selectOne(any(LambdaQueryWrapper.class), anyBoolean())).thenReturn(team);

        assertThrows(BusinessException.class, () -> teamService.joinByInviteCode(2L, "ABC123"));
    }

    @Test
    void joinByInviteCode_shouldThrowWhenAlreadyApprovedMember() {
        Team team = new Team();
        team.setId(1L);
        team.setLeaderId(1L);
        team.setTeamName("测试团队");
        team.setInviteCode("ABC123");
        team.setInviteCodeExpire(LocalDateTime.now().plusDays(7));
        team.setStatus(CommonConstants.TEAM_FORMING);
        when(teamMapper.selectOne(any(LambdaQueryWrapper.class), anyBoolean())).thenReturn(team);
        TeamMember existing = new TeamMember();
        existing.setUserId(2L);
        existing.setTeamId(1L);
        existing.setStatus(CommonConstants.MEMBER_APPROVED);
        when(teamMemberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existing);

        assertThrows(BusinessException.class, () -> teamService.joinByInviteCode(2L, "ABC123"));
    }

    @Test
    void joinByInviteCode_shouldResubmitWhenPreviouslyRejected() {
        Team team = new Team();
        team.setId(1L);
        team.setLeaderId(1L);
        team.setTeamName("测试团队");
        team.setInviteCode("ABC123");
        team.setInviteCodeExpire(LocalDateTime.now().plusDays(7));
        team.setStatus(CommonConstants.TEAM_FORMING);
        when(teamMapper.selectOne(any(LambdaQueryWrapper.class), anyBoolean())).thenReturn(team);
        TeamMember existing = new TeamMember();
        existing.setId(5L);
        existing.setUserId(2L);
        existing.setTeamId(1L);
        existing.setStatus(CommonConstants.MEMBER_REJECTED);
        when(teamMemberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existing);
        when(teamMemberMapper.updateById(any(TeamMember.class))).thenReturn(1);
        User user = new User();
        user.setName("张三");
        when(userService.getById(2L)).thenReturn(user);

        teamService.joinByInviteCode(2L, "ABC123");

        assertEquals(CommonConstants.MEMBER_PENDING, existing.getStatus());
        assertNull(existing.getHandleTime());
    }

    @Test
    void joinByInviteCode_shouldSucceed() {
        Team team = new Team();
        team.setId(1L);
        team.setLeaderId(1L);
        team.setTeamName("测试团队");
        team.setInviteCode("ABC123");
        team.setInviteCodeExpire(LocalDateTime.now().plusDays(7));
        team.setStatus(CommonConstants.TEAM_FORMING);
        when(teamMapper.selectOne(any(LambdaQueryWrapper.class), anyBoolean())).thenReturn(team);
        when(teamMemberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(teamMemberMapper.insert(any(TeamMember.class))).thenReturn(1);
        User user = new User();
        user.setName("张三");
        when(userService.getById(2L)).thenReturn(user);

        Team result = teamService.joinByInviteCode(2L, "ABC123");

        assertNotNull(result);
        verify(teamMemberMapper).insert(any(TeamMember.class));
        verify(notificationService).sendNotification(eq(1L), eq(CommonConstants.NOTIFY_TEAM_APPLY), anyString(), anyString(), anyLong(), anyString());
    }

    @Test
    void approveMember_shouldThrowWhenNotLeader() {
        Team team = new Team();
        team.setId(1L);
        team.setLeaderId(1L);
        when(teamMapper.selectById(1L)).thenReturn(team);

        assertThrows(BusinessException.class, () -> teamService.approveMember(1L, 2L, 1L));
    }

    @Test
    void approveMember_shouldThrowWhenMemberNotFound() {
        Team team = new Team();
        team.setId(1L);
        team.setLeaderId(1L);
        when(teamMapper.selectById(1L)).thenReturn(team);
        when(teamMemberMapper.selectById(1L)).thenReturn(null);
        when(registrationService.lambdaQuery()).thenReturn(mock(LambdaQueryChainWrapper.class,
                invocation -> {
                    if ("eq".equals(invocation.getMethod().getName())) return invocation.getMock();
                    if ("ne".equals(invocation.getMethod().getName())) return invocation.getMock();
                    if ("list".equals(invocation.getMethod().getName())) return List.of();
                    return null;
                }));

        assertThrows(BusinessException.class, () -> teamService.approveMember(1L, 1L, 1L));
    }

    @Test
    void approveMember_shouldSucceed() {
        Team team = new Team();
        team.setId(1L);
        team.setLeaderId(1L);
        team.setMemberCount(2);
        when(teamMapper.selectById(1L)).thenReturn(team);
        when(teamMapper.updateById(any(Team.class))).thenReturn(1);
        TeamMember member = new TeamMember();
        member.setId(1L);
        member.setTeamId(1L);
        member.setUserId(3L);
        member.setStatus(CommonConstants.MEMBER_PENDING);
        when(teamMemberMapper.selectById(1L)).thenReturn(member);
        when(teamMemberMapper.updateById(any(TeamMember.class))).thenReturn(1);
        when(registrationService.lambdaQuery()).thenReturn(mock(LambdaQueryChainWrapper.class,
                invocation -> {
                    if ("eq".equals(invocation.getMethod().getName())) return invocation.getMock();
                    if ("ne".equals(invocation.getMethod().getName())) return invocation.getMock();
                    if ("list".equals(invocation.getMethod().getName())) return List.of();
                    return null;
                }));

        teamService.approveMember(1L, 1L, 1L);

        assertEquals(CommonConstants.MEMBER_APPROVED, member.getStatus());
        assertNotNull(member.getHandleTime());
        assertEquals(3, team.getMemberCount());
    }

    @Test
    void rejectMember_shouldDecrementCountWhenWasApproved() {
        Team team = new Team();
        team.setId(1L);
        team.setLeaderId(1L);
        team.setMemberCount(3);
        when(teamMapper.selectById(1L)).thenReturn(team);
        when(teamMapper.updateById(any(Team.class))).thenReturn(1);
        TeamMember member = new TeamMember();
        member.setId(1L);
        member.setTeamId(1L);
        member.setStatus(CommonConstants.MEMBER_APPROVED);
        when(teamMemberMapper.selectById(1L)).thenReturn(member);
        when(teamMemberMapper.updateById(any(TeamMember.class))).thenReturn(1);

        teamService.rejectMember(1L, 1L, 1L);

        assertEquals(CommonConstants.MEMBER_REJECTED, member.getStatus());
        assertEquals(2, team.getMemberCount());
    }

    @Test
    void rejectMember_shouldNotDecrementWhenWasPending() {
        Team team = new Team();
        team.setId(1L);
        team.setLeaderId(1L);
        team.setMemberCount(2);
        when(teamMapper.selectById(1L)).thenReturn(team);
        TeamMember member = new TeamMember();
        member.setId(1L);
        member.setTeamId(1L);
        member.setStatus(CommonConstants.MEMBER_PENDING);
        when(teamMemberMapper.selectById(1L)).thenReturn(member);
        when(teamMemberMapper.updateById(any(TeamMember.class))).thenReturn(1);

        teamService.rejectMember(1L, 1L, 1L);

        assertEquals(CommonConstants.MEMBER_REJECTED, member.getStatus());
        assertEquals(2, team.getMemberCount());
    }

    @Test
    void removeMember_shouldThrowWhenNotLeader() {
        Team team = new Team();
        team.setId(1L);
        team.setLeaderId(1L);
        when(teamMapper.selectById(1L)).thenReturn(team);

        assertThrows(BusinessException.class, () -> teamService.removeMember(1L, 2L, 1L));
    }

    @Test
    void removeMember_shouldThrowWhenMemberNotFound() {
        Team team = new Team();
        team.setId(1L);
        team.setLeaderId(1L);
        when(teamMapper.selectById(1L)).thenReturn(team);
        when(teamMemberMapper.selectById(1L)).thenReturn(null);

        assertThrows(BusinessException.class, () -> teamService.removeMember(1L, 1L, 1L));
    }

    @Test
    void removeMember_shouldThrowWhenRemovingLeader() {
        Team team = new Team();
        team.setId(1L);
        team.setLeaderId(1L);
        when(teamMapper.selectById(1L)).thenReturn(team);
        TeamMember member = new TeamMember();
        member.setTeamId(1L);
        member.setRole(CommonConstants.MEMBER_LEADER);
        when(teamMemberMapper.selectById(1L)).thenReturn(member);

        assertThrows(BusinessException.class, () -> teamService.removeMember(1L, 1L, 1L));
    }

    @Test
    void removeMember_shouldSucceed() {
        Team team = new Team();
        team.setId(1L);
        team.setLeaderId(1L);
        team.setMemberCount(3);
        when(teamMapper.selectById(1L)).thenReturn(team);
        when(teamMapper.updateById(any(Team.class))).thenReturn(1);
        TeamMember member = new TeamMember();
        member.setId(1L);
        member.setTeamId(1L);
        member.setUserId(2L);
        member.setRole(CommonConstants.MEMBER_NORMAL);
        member.setStatus(CommonConstants.MEMBER_APPROVED);
        when(teamMemberMapper.selectById(1L)).thenReturn(member);
        when(teamMemberMapper.updateById(any(TeamMember.class))).thenReturn(1);

        teamService.removeMember(1L, 1L, 1L);

        assertEquals(CommonConstants.MEMBER_REJECTED, member.getStatus());
        assertEquals(2, team.getMemberCount());
    }

    @Test
    void dissolveTeam_shouldCancelRegistrationIfWasApproved() {
        Team team = new Team();
        team.setId(1L);
        team.setLeaderId(1L);
        team.setTeamName("测试团队");
        when(teamMapper.selectById(1L)).thenReturn(team);
        when(teamMapper.deleteById(1L)).thenReturn(1);
        when(teamMemberMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());
        when(registrationService.lambdaQuery()).thenReturn(mock(LambdaQueryChainWrapper.class,
                invocation -> {
                    if ("eq".equals(invocation.getMethod().getName())) return invocation.getMock();
                    if ("ne".equals(invocation.getMethod().getName())) return invocation.getMock();
                    if ("list".equals(invocation.getMethod().getName())) return List.of();
                    return null;
                }));

        teamService.dissolveTeam(1L, 1L);

        verify(teamMapper).deleteById(1L);
    }

    @Test
    void dissolveTeam_shouldNotifyMembers() {
        Team team = new Team();
        team.setId(1L);
        team.setLeaderId(1L);
        team.setTeamName("测试团队");
        when(teamMapper.selectById(1L)).thenReturn(team);
        when(teamMapper.deleteById(1L)).thenReturn(1);
        TeamMember m1 = new TeamMember();
        m1.setUserId(1L);
        m1.setStatus(CommonConstants.MEMBER_APPROVED);
        TeamMember m2 = new TeamMember();
        m2.setUserId(3L);
        m2.setStatus(CommonConstants.MEMBER_APPROVED);
        when(teamMemberMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(m1, m2));
        when(teamMemberMapper.updateById(any(TeamMember.class))).thenReturn(1);
        Registration pendingReg = new Registration();
        pendingReg.setId(10L);
        pendingReg.setTeamId(1L);
        pendingReg.setContestId(1L);
        pendingReg.setStatus(CommonConstants.REG_PENDING);
        when(registrationService.lambdaQuery()).thenReturn(mock(LambdaQueryChainWrapper.class,
                invocation -> {
                    if ("eq".equals(invocation.getMethod().getName())) return invocation.getMock();
                    if ("ne".equals(invocation.getMethod().getName())) return invocation.getMock();
                    if ("list".equals(invocation.getMethod().getName())) return List.of(pendingReg);
                    return null;
                }));

        teamService.dissolveTeam(1L, 1L);

        verify(notificationService).sendNotification(eq(3L), anyInt(), anyString(), anyString(), anyLong(), anyString());
        assertEquals(CommonConstants.MEMBER_REJECTED, m1.getStatus());
        assertEquals(CommonConstants.MEMBER_REJECTED, m2.getStatus());
        assertEquals(CommonConstants.REG_CANCELLED, pendingReg.getStatus());
    }

    @Test
    void leaveTeam_shouldThrowWhenNotMember() {
        when(teamMemberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        assertThrows(BusinessException.class, () -> teamService.leaveTeam(1L, 1L));
    }

    @Test
    void leaveTeam_shouldThrowWhenLeader() {
        TeamMember member = new TeamMember();
        member.setRole(CommonConstants.MEMBER_LEADER);
        when(teamMemberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(member);

        assertThrows(BusinessException.class, () -> teamService.leaveTeam(1L, 1L));
    }

    @Test
    void leaveTeam_shouldThrowWhenNotApprovedMember() {
        TeamMember member = new TeamMember();
        member.setRole(CommonConstants.MEMBER_NORMAL);
        member.setStatus(CommonConstants.MEMBER_PENDING);
        when(teamMemberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(member);

        assertThrows(BusinessException.class, () -> teamService.leaveTeam(1L, 1L));
    }

    @Test
    void leaveTeam_shouldSucceed() {
        Team team = new Team();
        team.setId(1L);
        team.setLeaderId(2L);
        team.setTeamName("测试团队");
        team.setMemberCount(2);
        when(teamMapper.selectById(1L)).thenReturn(team);
        when(teamMapper.updateById(any(Team.class))).thenReturn(1);
        TeamMember member = new TeamMember();
        member.setId(1L);
        member.setTeamId(1L);
        member.setUserId(3L);
        member.setRole(CommonConstants.MEMBER_NORMAL);
        member.setStatus(CommonConstants.MEMBER_APPROVED);
        when(teamMemberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(member);
        User user = new User();
        user.setName("张三");
        when(userService.getById(3L)).thenReturn(user);

        teamService.leaveTeam(1L, 3L);

        verify(teamMemberMapper).updateById(any(TeamMember.class));
        verify(teamMapper).updateById(team);
        assertEquals(1, team.getMemberCount());
    }

    @Test
    void submitForReview_shouldThrowWhenNotLeader() {
        Team team = new Team();
        team.setLeaderId(2L);
        when(teamMapper.selectById(1L)).thenReturn(team);

        assertThrows(BusinessException.class, () -> teamService.submitForReview(1L, 1L));
    }

    @Test
    void submitForReview_shouldThrowWhenTeamNotFound() {
        when(teamMapper.selectById(1L)).thenReturn(null);

        assertThrows(BusinessException.class, () -> teamService.submitForReview(1L, 1L));
    }

    @Test
    void submitForReview_shouldSucceed() {
        Team team = new Team();
        team.setId(1L);
        team.setLeaderId(1L);
        team.setTeamName("测试团队");
        team.setStatus(CommonConstants.TEAM_FORMING);
        team.setMemberCount(2);
        when(teamMapper.selectById(1L)).thenReturn(team);
        when(teamMapper.updateById(any(Team.class))).thenReturn(1);
        when(registrationService.lambdaQuery()).thenReturn(mock(LambdaQueryChainWrapper.class,
                invocation -> {
                    if ("eq".equals(invocation.getMethod().getName())) return invocation.getMock();
                    if ("ne".equals(invocation.getMethod().getName())) return invocation.getMock();
                    if ("list".equals(invocation.getMethod().getName())) return List.of();
                    return null;
                }));
        when(teamMemberMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        teamService.submitForReview(1L, 1L);

        assertEquals(CommonConstants.TEAM_SUBMITTED, team.getStatus());
    }

    @Test
    void getTeamsByLeader_shouldReturnTeams() {
        Team team1 = new Team();
        team1.setId(1L);
        team1.setLeaderId(1L);
        when(teamMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(team1));

        List<Team> result = teamService.getTeamsByLeader(1L);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getLeaderId());
    }

    @Test
    void getTeamsByLeader_shouldReturnEmptyWhenNone() {
        when(teamMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());

        List<Team> result = teamService.getTeamsByLeader(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    void adminApproveTeam_shouldThrowWhenNotSubmitted() {
        Team team = new Team();
        team.setStatus(CommonConstants.TEAM_FORMING);
        when(teamMapper.selectById(1L)).thenReturn(team);

        assertThrows(BusinessException.class, () -> teamService.adminApproveTeam(1L));
    }

    @Test
    void adminApproveTeam_shouldThrowWhenTeamNotFound() {
        when(teamMapper.selectById(1L)).thenReturn(null);

        assertThrows(BusinessException.class, () -> teamService.adminApproveTeam(1L));
    }

    @Test
    void adminApproveTeam_shouldApprove() {
        Team team = new Team();
        team.setId(1L);
        team.setLeaderId(1L);
        team.setTeamName("测试团队");
        team.setStatus(CommonConstants.TEAM_SUBMITTED);
        when(teamMapper.selectById(1L)).thenReturn(team);
        when(teamMapper.updateById(any(Team.class))).thenReturn(1);
        when(registrationService.lambdaQuery()).thenReturn(mock(LambdaQueryChainWrapper.class,
                invocation -> {
                    if ("eq".equals(invocation.getMethod().getName())) return invocation.getMock();
                    if ("one".equals(invocation.getMethod().getName())) return null;
                    return null;
                }));

        teamService.adminApproveTeam(1L);

        assertEquals(CommonConstants.TEAM_APPROVED, team.getStatus());
    }

    @Test
    void adminRejectTeam_shouldThrowWhenReasonTooShort() {
        assertThrows(BusinessException.class, () -> teamService.adminRejectTeam(1L, "否"));
    }

    @Test
    void adminRejectTeam_shouldThrowWhenNullReason() {
        assertThrows(BusinessException.class, () -> teamService.adminRejectTeam(1L, null));
    }

    @Test
    void adminRejectTeam_shouldSucceed() {
        Team team = new Team();
        team.setId(1L);
        team.setLeaderId(1L);
        team.setTeamName("测试团队");
        team.setStatus(CommonConstants.TEAM_SUBMITTED);
        team.setMemberCount(2);
        when(teamMapper.selectById(1L)).thenReturn(team);
        when(teamMapper.updateById(any(Team.class))).thenReturn(1);
        TeamMember m1 = new TeamMember();
        m1.setUserId(1L);
        m1.setStatus(CommonConstants.MEMBER_APPROVED);
        TeamMember m2 = new TeamMember();
        m2.setUserId(2L);
        m2.setStatus(CommonConstants.MEMBER_PENDING);
        when(teamMemberMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(m1, m2));
        when(teamMemberMapper.updateById(any(TeamMember.class))).thenReturn(1);

        Registration pendingReg = new Registration();
        pendingReg.setId(10L);
        pendingReg.setTeamId(1L);
        pendingReg.setStatus(CommonConstants.REG_PENDING);
        when(registrationService.lambdaQuery()).thenReturn(mock(LambdaQueryChainWrapper.class,
                invocation -> {
                    if ("eq".equals(invocation.getMethod().getName())) return invocation.getMock();
                    if ("one".equals(invocation.getMethod().getName())) return pendingReg;
                    return null;
                }));

        teamService.adminRejectTeam(1L, "不合规，请修改后重新提交");

        assertEquals(CommonConstants.TEAM_REJECTED, team.getStatus());
        assertEquals(0, team.getMemberCount());
        assertEquals(CommonConstants.MEMBER_REJECTED, m1.getStatus());
        assertEquals(CommonConstants.REG_REJECTED, pendingReg.getStatus());
        verify(registrationService).updateById(pendingReg);
    }

    @Test
    void listUserTeams_shouldReturnTeams() {
        TeamMember member = new TeamMember();
        member.setTeamId(1L);
        member.setUserId(1L);
        member.setStatus(CommonConstants.MEMBER_APPROVED);
        when(teamMemberMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(member));
        Team team = new Team();
        team.setId(1L);
        when(teamMapper.selectByIds(anyList())).thenReturn(List.of(team));

        List<Team> result = teamService.listUserTeams(1L);

        assertEquals(1, result.size());
    }

    @Test
    void listUserTeams_shouldReturnEmptyWhenNoMembership() {
        when(teamMemberMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());

        List<Team> result = teamService.listUserTeams(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    void listMembers_shouldReturnApprovedMembers() {
        TeamMember m = new TeamMember();
        m.setUserId(1L);
        m.setStatus(CommonConstants.MEMBER_APPROVED);
        when(teamMemberMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(m));
        User user = new User();
        user.setId(1L);
        user.setName("张三");
        when(userService.listByIds(anyList())).thenReturn(List.of(user));

        List<TeamMember> result = teamService.listMembers(1L);

        assertEquals(1, result.size());
        assertEquals("张三", result.get(0).getUserName());
    }

    @Test
    void listPendingMembers_shouldReturnPendingMembers() {
        TeamMember m = new TeamMember();
        m.setUserId(1L);
        m.setStatus(CommonConstants.MEMBER_PENDING);
        when(teamMemberMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(m));
        User user = new User();
        user.setId(1L);
        user.setName("张三");
        when(userService.listByIds(anyList())).thenReturn(List.of(user));

        List<TeamMember> result = teamService.listPendingMembers(1L);

        assertEquals(1, result.size());
    }

    @Test
    void pageTeams_shouldReturnPagedResult() {
        Team team = new Team();
        team.setId(1L);
        var page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<Team>();
        page.setRecords(List.of(team));
        page.setTotal(1);
        when(teamMapper.selectPage(any(), any(LambdaQueryWrapper.class))).thenReturn(page);

        var result = teamService.pageTeams(null, 1, 10);

        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
    }
}
