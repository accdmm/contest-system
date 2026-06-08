package com.contest.team.controller;

import com.contest.team.service.TeamService;
import com.contest.team.test.TestApplication;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = TestApplication.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class TeamControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    private void setAuthUser(Long userId) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication auth = new UsernamePasswordAuthenticationToken(userId, null, java.util.List.of());
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
    }

    @BeforeEach
    void setUp() {
        jdbc.execute("INSERT INTO `user` (id, username, password, name, role, status) VALUES (1, 'leader', 'pw', '队长', 0, 0)");
        jdbc.execute("INSERT INTO `user` (id, username, password, name, role, status) VALUES (2, 'member', 'pw', '成员A', 0, 0)");
        jdbc.execute("INSERT INTO `user` (id, username, password, name, role, status) VALUES (3, 'applicant', 'pw', '申请人', 0, 0)");
        jdbc.execute("INSERT INTO `user` (id, username, password, name, role, status) VALUES (13, 'teacher', 'pw', '王教授', 2, 0)");
        jdbc.execute("INSERT INTO contest (id, name, contest_type, team_min_size, team_max_size, max_participants, status, register_start_time, register_end_time, contest_time) VALUES (1, '测试竞赛', 1, 2, 5, 100, 1, '2026-01-01 00:00:00', '2026-12-31 23:59:59', '2027-01-01 00:00:00')");
        jdbc.execute("INSERT INTO team (id, leader_id, team_name, team_no, status, member_count, invite_code, invite_code_expire) VALUES (1, 1, '测试团队', 'T00001', 0, 1, 'ABC123', '2026-12-31 23:59:59')");
        jdbc.execute("INSERT INTO team_member (team_id, user_id, role, status, apply_time) VALUES (1, 1, 1, 1, NOW())");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getById_shouldReturnTeam() throws Exception {
        mockMvc.perform(get("/api/team/1/detail"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.teamName").value("测试团队"));
    }

    @Test
    void getById_shouldReturnErrorWhenNotFound() throws Exception {
        mockMvc.perform(get("/api/team/999/detail"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void getByLeader_shouldReturnTeams() throws Exception {
        setAuthUser(1L);
        mockMvc.perform(get("/api/team/leader?userId=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].leaderId").value(1));
    }

    @Test
    void members_shouldReturnList() throws Exception {
        mockMvc.perform(get("/api/team/1/members"))
                .andExpect(status().isOk());
    }

    @Test
    void pendingMembers_shouldReturnList() throws Exception {
        mockMvc.perform(get("/api/team/1/pending"))
                .andExpect(status().isOk());
    }

    @Test
    void userTeams_shouldReturnList() throws Exception {
        mockMvc.perform(get("/api/team/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(1));
    }

    @Test
    void page_shouldReturnPagedResult() throws Exception {
        mockMvc.perform(get("/api/team/page")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void generateInvite_shouldReturnCode() throws Exception {
        setAuthUser(1L);
        mockMvc.perform(post("/api/team/1/invite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void createTeam_shouldSucceed() throws Exception {
        setAuthUser(2L);
        mockMvc.perform(post("/api/team")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"teamName\": \"新建团队\", \"userId\": 2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.teamName").value("新建团队"));
    }

    @Test
    void joinTeam_shouldSucceed() throws Exception {
        setAuthUser(3L);
        mockMvc.perform(post("/api/team/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"inviteCode\": \"ABC123\", \"userId\": 3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void approveMember_shouldSucceed() throws Exception {
        setAuthUser(1L);
        jdbc.execute("INSERT INTO team_member (team_id, user_id, role, status, apply_time) VALUES (1, 3, 0, 0, NOW())");
        Long memberId = jdbc.queryForObject("SELECT id FROM team_member WHERE team_id = 1 AND user_id = 3", Long.class);
        mockMvc.perform(post("/api/team/1/members/" + memberId + "/approve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void rejectMember_shouldSucceed() throws Exception {
        setAuthUser(1L);
        jdbc.execute("INSERT INTO team_member (team_id, user_id, role, status, apply_time) VALUES (1, 3, 0, 0, NOW())");
        Long memberId = jdbc.queryForObject("SELECT id FROM team_member WHERE team_id = 1 AND user_id = 3", Long.class);
        mockMvc.perform(post("/api/team/1/members/" + memberId + "/reject"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void removeMember_shouldSucceed() throws Exception {
        setAuthUser(1L);
        jdbc.execute("INSERT INTO team_member (team_id, user_id, role, status, apply_time) VALUES (1, 3, 0, 1, NOW())");
        jdbc.execute("UPDATE team SET member_count = 2 WHERE id = 1");
        Long memberId = jdbc.queryForObject("SELECT id FROM team_member WHERE team_id = 1 AND user_id = 3", Long.class);
        mockMvc.perform(post("/api/team/1/members/" + memberId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void leaveTeam_shouldSucceed() throws Exception {
        setAuthUser(3L);
        jdbc.execute("INSERT INTO team_member (team_id, user_id, role, status, apply_time) VALUES (1, 3, 0, 1, NOW())");
        jdbc.execute("UPDATE team SET member_count = 2 WHERE id = 1");
        mockMvc.perform(post("/api/team/1/leave"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void dissolveTeam_shouldSucceed() throws Exception {
        setAuthUser(1L);
        mockMvc.perform(post("/api/team/1/dissolve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void submitReview_shouldSucceed() throws Exception {
        setAuthUser(1L);
        jdbc.execute("INSERT INTO team_member (team_id, user_id, role, status, apply_time, handle_time) VALUES (1, 2, 0, 1, NOW(), NOW())");
        jdbc.execute("UPDATE team SET member_count = 2 WHERE id = 1");
        jdbc.execute("INSERT INTO registration (contest_id, user_id, team_id, reg_type, status) VALUES (1, 1, 1, 1, 0)");
        mockMvc.perform(post("/api/team/1/submit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void adminApprove_shouldSucceed() throws Exception {
        jdbc.execute("UPDATE team SET status = 1 WHERE id = 1");
        mockMvc.perform(post("/api/team/1/admin-approve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void adminReject_shouldSucceed() throws Exception {
        jdbc.execute("UPDATE team SET status = 1 WHERE id = 1");
        mockMvc.perform(post("/api/team/1/admin-reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\": \"材料不符合要求，请修改后重新提交\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
