package com.contest.register.service.impl;

import com.contest.common.constant.CommonConstants;
import com.contest.common.exception.BusinessException;
import com.contest.competition.entity.Contest;
import com.contest.competition.service.ContestService;
import com.contest.register.entity.Registration;
import com.contest.register.service.RegistrationService;
import com.contest.register.test.TestApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = TestApplication.class)
@ActiveProfiles("test")
@Transactional
class RegistrationServiceImplTest {

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private ContestService contestService;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        jdbc.execute("INSERT INTO `user` (id, username, password, name, role, status) VALUES (1, 'student', 'pw', '学生A', 0, 0)");
        jdbc.execute("INSERT INTO `user` (id, username, password, name, role, status) VALUES (2, 'student2', 'pw', '学生B', 0, 0)");
        jdbc.execute("INSERT INTO contest (id, name, contest_type, max_participants, current_count, status, register_start_time, register_end_time, contest_time) VALUES (1, '个人赛', 0, 100, 0, 1, '2026-01-01 00:00:00', '2026-12-31 23:59:59', '2027-01-01 00:00:00')");
        jdbc.execute("INSERT INTO contest (id, name, contest_type, max_participants, current_count, status, register_start_time, register_end_time, contest_time) VALUES (2, '团队赛', 1, 100, 0, 1, '2026-01-01 00:00:00', '2026-12-31 23:59:59', '2027-01-01 00:00:00')");
        jdbc.execute("INSERT INTO team (id, leader_id, team_name, team_no, status, member_count) VALUES (1, 1, '测试团队', 'T00001', 2, 3)");
        jdbc.execute("INSERT INTO team_member (team_id, user_id, role, status, apply_time, handle_time) VALUES (1, 1, 1, 1, NOW(), NOW())");
        jdbc.execute("INSERT INTO team_member (team_id, user_id, role, status, apply_time, handle_time) VALUES (1, 2, 0, 1, NOW(), NOW())");
    }

    // ==================== registerPersonal ====================

    @Test
    void registerPersonal_shouldThrowWhenContestNotFound() {
        assertThrows(BusinessException.class, () -> registrationService.registerPersonal(1L, 999L, ""));
    }

    @Test
    void registerPersonal_shouldThrowWhenContestNotOpen() {
        jdbc.execute("UPDATE contest SET status = 0 WHERE id = 1");
        assertThrows(BusinessException.class, () -> registrationService.registerPersonal(1L, 1L, ""));
    }

    @Test
    void registerPersonal_shouldThrowWhenContestIsTeamOnly() {
        assertThrows(BusinessException.class, () -> registrationService.registerPersonal(1L, 2L, ""));
    }

    @Test
    void registerPersonal_shouldThrowWhenAlreadyRegistered() {
        jdbc.execute("INSERT INTO registration (contest_id, user_id, reg_type, status) VALUES (1, 1, 0, 0)");
        assertThrows(BusinessException.class, () -> registrationService.registerPersonal(1L, 1L, ""));
    }

    @Test
    void registerPersonal_shouldThrowWhenContestFull() {
        jdbc.execute("UPDATE contest SET max_participants = 1 WHERE id = 1");
        jdbc.execute("INSERT INTO registration (contest_id, user_id, reg_type, status) VALUES (1, 2, 0, 1)");
        assertThrows(BusinessException.class, () -> registrationService.registerPersonal(1L, 1L, ""));
    }

    @Test
    void registerPersonal_shouldThrowWhenExceedsMaxActive() {
        jdbc.execute("INSERT INTO registration (contest_id, user_id, reg_type, status) VALUES (2, 1, 0, 0)");
        jdbc.execute("INSERT INTO registration (contest_id, user_id, reg_type, status) VALUES (3, 1, 0, 0)");
        jdbc.execute("INSERT INTO registration (contest_id, user_id, reg_type, status) VALUES (4, 1, 0, 0)");
        assertThrows(BusinessException.class, () -> registrationService.registerPersonal(1L, 1L, ""));
    }

    @Test
    void registerPersonal_shouldThrowWhenRegistrationNotStarted() {
        jdbc.execute("UPDATE contest SET register_start_time = '2099-01-01 00:00:00' WHERE id = 1");
        assertThrows(BusinessException.class, () -> registrationService.registerPersonal(1L, 1L, ""));
    }

    @Test
    void registerPersonal_shouldThrowWhenRegistrationEnded() {
        jdbc.execute("UPDATE contest SET register_end_time = '2020-01-01 00:00:00' WHERE id = 1");
        assertThrows(BusinessException.class, () -> registrationService.registerPersonal(1L, 1L, ""));
    }

    @Test
    void registerPersonal_shouldSucceed() {
        Registration result = registrationService.registerPersonal(1L, 1L, "参赛");
        assertNotNull(result);
        assertEquals(1L, result.getContestId());
        assertEquals(1L, result.getUserId());
        assertEquals(CommonConstants.REG_PERSONAL, result.getRegType());
        assertEquals(CommonConstants.REG_PENDING, result.getStatus());
        assertEquals("参赛", result.getRemark());
    }

    // ==================== registerTeam ====================

    @Test
    void registerTeam_shouldThrowWhenContestNotOpen() {
        jdbc.execute("UPDATE contest SET status = 2 WHERE id = 2");
        assertThrows(BusinessException.class, () -> registrationService.registerTeam(1L, 2L, 1L));
    }

    @Test
    void registerTeam_shouldThrowWhenContestIsPersonalOnly() {
        assertThrows(BusinessException.class, () -> registrationService.registerTeam(1L, 1L, 1L));
    }

    @Test
    void registerTeam_shouldThrowWhenNotLeader() {
        assertThrows(BusinessException.class, () -> registrationService.registerTeam(2L, 2L, 1L));
    }

    @Test
    void registerTeam_shouldThrowWhenTeamAlreadyRegistered() {
        registrationService.registerTeam(1L, 2L, 1L);
        assertThrows(BusinessException.class, () -> registrationService.registerTeam(1L, 2L, 1L));
    }

    @Test
    void registerTeam_shouldSucceed() {
        Registration result = registrationService.registerTeam(1L, 2L, 1L);
        assertNotNull(result);
        assertEquals(CommonConstants.REG_TEAM, result.getRegType());
        assertEquals(1L, result.getTeamId());
    }

    // ==================== cancelRegistration ====================

    @Test
    void cancelRegistration_shouldThrowWhenNotFound() {
        assertThrows(BusinessException.class, () -> registrationService.cancelRegistration(999L, 1L));
    }

    @Test
    void cancelRegistration_shouldThrowWhenNotOwner() {
        jdbc.execute("INSERT INTO registration (id, contest_id, user_id, reg_type, status) VALUES (10, 1, 1, 0, 0)");
        assertThrows(BusinessException.class, () -> registrationService.cancelRegistration(10L, 2L));
    }

    @Test
    void cancelRegistration_shouldThrowWhenAlreadyCancelled() {
        jdbc.execute("INSERT INTO registration (id, contest_id, user_id, reg_type, status) VALUES (10, 1, 1, 0, 3)");
        assertThrows(BusinessException.class, () -> registrationService.cancelRegistration(10L, 1L));
    }

    @Test
    void cancelRegistration_shouldDecrementWhenWasApproved() {
        jdbc.execute("INSERT INTO registration (id, contest_id, user_id, reg_type, status) VALUES (10, 1, 1, 0, 1)");
        jdbc.execute("UPDATE contest SET current_count = 1 WHERE id = 1");
        registrationService.cancelRegistration(10L, 1L);
        Integer status = jdbc.queryForObject("SELECT status FROM registration WHERE id = 10", Integer.class);
        assertEquals(CommonConstants.REG_CANCELLED, status.intValue());
        Integer count = jdbc.queryForObject("SELECT current_count FROM contest WHERE id = 1", Integer.class);
        assertEquals(0, count.intValue());
    }

    @Test
    void cancelRegistration_shouldSucceed() {
        jdbc.execute("INSERT INTO registration (id, contest_id, user_id, reg_type, status) VALUES (10, 1, 1, 0, 0)");
        registrationService.cancelRegistration(10L, 1L);
        Integer status = jdbc.queryForObject("SELECT status FROM registration WHERE id = 10", Integer.class);
        assertEquals(CommonConstants.REG_CANCELLED, status.intValue());
    }

    // ==================== approveRegistration ====================

    @Test
    void approveRegistration_shouldThrowWhenNotFound() {
        assertThrows(BusinessException.class, () -> registrationService.approveRegistration(999L));
    }

    @Test
    void approveRegistration_shouldThrowWhenAlreadyApproved() {
        jdbc.execute("INSERT INTO registration (id, contest_id, user_id, reg_type, status) VALUES (10, 1, 1, 0, 1)");
        assertThrows(BusinessException.class, () -> registrationService.approveRegistration(10L));
    }

    @Test
    void approveRegistration_shouldHandleNullCurrentCount() {
        jdbc.execute("UPDATE contest SET current_count = NULL WHERE id = 1");
        jdbc.execute("INSERT INTO registration (id, contest_id, user_id, reg_type, status) VALUES (10, 1, 1, 0, 0)");
        registrationService.approveRegistration(10L);
        Integer count = jdbc.queryForObject("SELECT current_count FROM contest WHERE id = 1", Integer.class);
        assertEquals(1, count.intValue());
    }

    @Test
    void approveRegistration_shouldSucceed() {
        jdbc.execute("INSERT INTO registration (id, contest_id, user_id, reg_type, status) VALUES (10, 1, 1, 0, 0)");
        registrationService.approveRegistration(10L);
        Integer status = jdbc.queryForObject("SELECT status FROM registration WHERE id = 10", Integer.class);
        assertEquals(CommonConstants.REG_APPROVED, status.intValue());
        Integer count = jdbc.queryForObject("SELECT current_count FROM contest WHERE id = 1", Integer.class);
        assertEquals(1, count.intValue());
    }

    // ==================== rejectRegistration ====================

    @Test
    void rejectRegistration_shouldThrowWhenReasonTooShort() {
        assertThrows(BusinessException.class, () -> registrationService.rejectRegistration(1L, "否"));
    }

    @Test
    void rejectRegistration_shouldThrowWhenNullReason() {
        assertThrows(BusinessException.class, () -> registrationService.rejectRegistration(1L, null));
    }

    @Test
    void rejectRegistration_shouldThrowWhenAlreadyRejected() {
        jdbc.execute("INSERT INTO registration (id, contest_id, user_id, reg_type, status) VALUES (10, 1, 1, 0, 2)");
        assertThrows(BusinessException.class, () -> registrationService.rejectRegistration(10L, "材料不全，请补充后重新提交"));
    }

    @Test
    void rejectRegistration_shouldDecrementCountWhenWasApproved() {
        jdbc.execute("INSERT INTO registration (id, contest_id, user_id, reg_type, status) VALUES (10, 1, 1, 0, 1)");
        jdbc.execute("UPDATE contest SET current_count = 1 WHERE id = 1");
        registrationService.rejectRegistration(10L, "材料不全，请补充后重新提交");
        Integer status = jdbc.queryForObject("SELECT status FROM registration WHERE id = 10", Integer.class);
        assertEquals(CommonConstants.REG_REJECTED, status.intValue());
        Integer count = jdbc.queryForObject("SELECT current_count FROM contest WHERE id = 1", Integer.class);
        assertEquals(0, count.intValue());
    }

    @Test
    void rejectRegistration_shouldSucceed() {
        jdbc.execute("INSERT INTO registration (id, contest_id, user_id, reg_type, status) VALUES (10, 1, 1, 0, 0)");
        registrationService.rejectRegistration(10L, "材料不全，请补充后重新提交");
        Integer status = jdbc.queryForObject("SELECT status FROM registration WHERE id = 10", Integer.class);
        assertEquals(CommonConstants.REG_REJECTED, status.intValue());
    }

    // ==================== pageByUser ====================

    @Test
    void pageByUser_shouldReturnPagedResult() {
        jdbc.execute("INSERT INTO registration (contest_id, user_id, reg_type, status) VALUES (1, 1, 0, 0)");
        var page = registrationService.pageByUser(1L, 1, 10);
        assertEquals(1, page.getTotal());
        assertNotNull(page.getRecords().get(0).getContestName());
    }

    // ==================== pageByContest ====================

    @Test
    void pageByContest_shouldReturnPagedResult() {
        jdbc.execute("INSERT INTO registration (contest_id, user_id, reg_type, status) VALUES (1, 1, 0, 0)");
        var page = registrationService.pageByContest(1L, 1, 10, null);
        assertEquals(1, page.getTotal());
    }

    @Test
    void pageByContest_shouldFilterByStatus() {
        jdbc.execute("INSERT INTO registration (contest_id, user_id, reg_type, status) VALUES (1, 1, 0, 0)");
        jdbc.execute("INSERT INTO registration (contest_id, user_id, reg_type, status) VALUES (1, 2, 0, 1)");
        var page = registrationService.pageByContest(1L, 1, 10, CommonConstants.REG_PENDING);
        assertEquals(1, page.getTotal());
    }

    // ==================== pageAll ====================

    @Test
    void pageAll_shouldReturnPagedResult() {
        jdbc.execute("INSERT INTO registration (contest_id, user_id, reg_type, status) VALUES (1, 1, 0, 0)");
        var page = registrationService.pageAll(null, null, 1, 10);
        assertEquals(1, page.getTotal());
    }
}
