package com.contest.team.controller;

import com.contest.team.entity.TeamDO;
import com.contest.team.entity.TeamMemberDO;
import com.contest.team.service.TeamService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TeamControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TeamService teamService;

    @BeforeEach
    void setUp() {
        TeamController controller = new TeamController(teamService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void create_shouldReturnTeam() throws Exception {
        TeamDO team = new TeamDO();
        team.setId(1L);
        team.setTeamName("测试团队");
        lenient().when(teamService.createTeam(any(), anyString(), any())).thenReturn(team);

        mockMvc.perform(post("/api/team")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"teamName\": \"测试团队\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.teamName").value("测试团队"));
    }

    @Test
    void create_shouldReturnErrorWhenMissingParams() throws Exception {
        mockMvc.perform(post("/api/team")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\": 1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void getByLeader_shouldReturnTeams() throws Exception {
        TeamDO team = new TeamDO();
        team.setId(1L);
        team.setLeaderId(1L);
        lenient().when(teamService.getTeamsByLeader(any())).thenReturn(List.of(team));

        mockMvc.perform(get("/api/team/leader"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(1));
    }

    @Test
    void getById_shouldReturnTeam() throws Exception {
        TeamDO team = new TeamDO();
        team.setId(1L);
        team.setTeamName("测试团队");
        when(teamService.getById(1L)).thenReturn(team);

        mockMvc.perform(get("/api/team/1/detail"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.teamName").value("测试团队"));
    }

    @Test
    void getById_shouldReturnErrorWhenNotFound() throws Exception {
        when(teamService.getById(999L)).thenReturn(null);

        mockMvc.perform(get("/api/team/999/detail"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void generateInvite_shouldReturnCode() throws Exception {
        lenient().when(teamService.generateInviteCode(any(), any())).thenReturn("ABC123");

        mockMvc.perform(post("/api/team/1/invite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("ABC123"));
    }

    @Test
    void generateInvite_shouldSucceedWithoutUserId() throws Exception {
        lenient().when(teamService.generateInviteCode(any(), any())).thenReturn("CODE123");
        mockMvc.perform(post("/api/team/1/invite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void join_shouldReturnTeam() throws Exception {
        TeamDO team = new TeamDO();
        team.setId(1L);
        lenient().when(teamService.joinByInviteCode(any(), anyString())).thenReturn(team);

        mockMvc.perform(post("/api/team/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"inviteCode\": \"ABC123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void join_shouldReturnErrorWhenMissingParams() throws Exception {
        mockMvc.perform(post("/api/team/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\": 1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void dissolve_shouldSucceed() throws Exception {
        mockMvc.perform(put("/api/team/1/dissolve").param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void submitReview_shouldSucceed() throws Exception {
        mockMvc.perform(put("/api/team/1/submit").param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void approveMember_shouldSucceed() throws Exception {
        mockMvc.perform(put("/api/team/1/members/2/approve").param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void rejectMember_shouldSucceed() throws Exception {
        mockMvc.perform(put("/api/team/1/members/3/reject").param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void removeMember_shouldSucceed() throws Exception {
        mockMvc.perform(delete("/api/team/1/members/4").param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void leave_shouldSucceed() throws Exception {
        mockMvc.perform(put("/api/team/1/leave").param("userId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void members_shouldReturnList() throws Exception {
        TeamMemberDO member = new TeamMemberDO();
        member.setId(1L);
        member.setUserId(2L);
        when(teamService.listMembers(1L)).thenReturn(List.of(member));

        mockMvc.perform(get("/api/team/1/members"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].userId").value(2));
    }

    @Test
    void pendingMembers_shouldReturnList() throws Exception {
        TeamMemberDO member = new TeamMemberDO();
        member.setId(1L);
        member.setStatus(0);
        when(teamService.listPendingMembers(1L)).thenReturn(List.of(member));

        mockMvc.perform(get("/api/team/1/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].status").value(0));
    }

    @Test
    void adminApprove_shouldSucceed() throws Exception {
        mockMvc.perform(put("/api/team/1/admin-approve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void adminReject_shouldSucceed() throws Exception {
        mockMvc.perform(put("/api/team/1/admin-reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\": \"材料不符合要求，请修改后重新提交\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void userTeams_shouldReturnList() throws Exception {
        TeamDO team = new TeamDO();
        team.setId(1L);
        when(teamService.listUserTeams(1L)).thenReturn(List.of(team));

        mockMvc.perform(get("/api/team/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(1));
    }

    @Test
    void page_shouldReturnPagedResult() throws Exception {
        TeamDO team = new TeamDO();
        team.setId(1L);
        when(teamService.pageTeams(any(), anyInt(), anyInt())).thenReturn(
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<TeamDO>() {{
                    setRecords(List.of(team));
                    setTotal(1);
                }}
        );

        mockMvc.perform(get("/api/team/page")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[0].id").value(1));
    }
}
