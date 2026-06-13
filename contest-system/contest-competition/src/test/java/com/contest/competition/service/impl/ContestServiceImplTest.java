package com.contest.competition.service.impl;

import com.contest.common.constant.CommonConstants;
import com.contest.common.exception.BusinessException;
import com.contest.competition.entity.Contest;
import com.contest.competition.service.ContestService;
import com.contest.competition.test.TestApplication;
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
class ContestServiceImplTest {

    @Autowired
    private ContestService contestService;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        jdbc.execute("INSERT INTO `user` (id, username, password, name, role, status) VALUES (1, 'admin', 'pw', '管理员', 1, 0)");
    }

    @Test
    void createContest_shouldThrowWhenTimeOrderInvalid() {
        Contest contest = new Contest();
        contest.setName("测试竞赛");
        contest.setRegisterStartTime(java.time.LocalDateTime.now().plusDays(5));
        contest.setRegisterEndTime(java.time.LocalDateTime.now().plusDays(3));
        contest.setContestTime(java.time.LocalDateTime.now().plusDays(10));
        assertThrows(BusinessException.class, () -> contestService.createContest(contest));
    }

    @Test
    void createContest_shouldThrowWhenContestTimeInPast() {
        Contest contest = new Contest();
        contest.setName("测试竞赛");
        contest.setRegisterStartTime(java.time.LocalDateTime.now().minusDays(10));
        contest.setRegisterEndTime(java.time.LocalDateTime.now().plusDays(3));
        contest.setContestTime(java.time.LocalDateTime.now().minusDays(5));
        assertThrows(BusinessException.class, () -> contestService.createContest(contest));
    }

    @Test
    void createContest_shouldSucceed() {
        Contest contest = new Contest();
        contest.setName("测试竞赛");
        contest.setCategory("理工类");
        contest.setLevel("校级");
        contest.setContestType(CommonConstants.CONTEST_PERSONAL);
        contest.setRegisterStartTime(java.time.LocalDateTime.now().plusDays(1));
        contest.setRegisterEndTime(java.time.LocalDateTime.now().plusDays(30));
        contest.setContestTime(java.time.LocalDateTime.now().plusDays(60));
        Contest result = contestService.createContest(contest);
        assertNotNull(result.getId());
        assertEquals("测试竞赛", result.getName());
        assertEquals(CommonConstants.CONTEST_DRAFT, result.getStatus());
    }

    @Test
    void publishContest_shouldSucceed() {
        Contest contest = new Contest();
        contest.setName("测试竞赛");
        contest.setContestType(CommonConstants.CONTEST_PERSONAL);
        contest.setRegisterStartTime(java.time.LocalDateTime.now().plusDays(1));
        contest.setRegisterEndTime(java.time.LocalDateTime.now().plusDays(30));
        contest.setContestTime(java.time.LocalDateTime.now().plusDays(60));
        Contest created = contestService.createContest(contest);
        contestService.publishContest(created.getId());
        Contest published = contestService.getById(created.getId());
        assertEquals(CommonConstants.CONTEST_OPEN, published.getStatus());
    }

    @Test
    void unpublishContest_shouldThrowWhenHasRegistrations() {
        Contest contest = new Contest();
        contest.setName("测试竞赛");
        contest.setContestType(CommonConstants.CONTEST_PERSONAL);
        contest.setRegisterStartTime(java.time.LocalDateTime.now().plusDays(1));
        contest.setRegisterEndTime(java.time.LocalDateTime.now().plusDays(30));
        contest.setContestTime(java.time.LocalDateTime.now().plusDays(60));
        Contest created = contestService.createContest(contest);
        Long contestId = created.getId();
        contestService.publishContest(contestId);
        jdbc.execute("INSERT INTO registration (contest_id, user_id, reg_type, status) VALUES (" + contestId + ", 1, 0, 1)");
        jdbc.execute("UPDATE contest SET current_count = 1 WHERE id = " + contestId);
        assertThrows(BusinessException.class, () -> contestService.unpublishContest(contestId));
    }

    @Test
    void unpublishContest_shouldSucceed() {
        Contest contest = new Contest();
        contest.setName("测试竞赛");
        contest.setContestType(CommonConstants.CONTEST_PERSONAL);
        contest.setRegisterStartTime(java.time.LocalDateTime.now().plusDays(1));
        contest.setRegisterEndTime(java.time.LocalDateTime.now().plusDays(30));
        contest.setContestTime(java.time.LocalDateTime.now().plusDays(60));
        Contest created = contestService.createContest(contest);
        Long contestId = created.getId();
        contestService.publishContest(contestId);
        contestService.unpublishContest(contestId);
        Contest unpublished = contestService.getById(contestId);
        assertEquals(CommonConstants.CONTEST_DRAFT, unpublished.getStatus());
    }

    @Test
    void deleteContest_shouldThrowWhenNotDraft() {
        Contest contest = new Contest();
        contest.setName("测试竞赛");
        contest.setContestType(CommonConstants.CONTEST_PERSONAL);
        contest.setRegisterStartTime(java.time.LocalDateTime.now().plusDays(1));
        contest.setRegisterEndTime(java.time.LocalDateTime.now().plusDays(30));
        contest.setContestTime(java.time.LocalDateTime.now().plusDays(60));
        Contest created = contestService.createContest(contest);
        Long contestId = created.getId();
        contestService.publishContest(contestId);
        assertThrows(BusinessException.class, () -> contestService.deleteContest(contestId));
    }

    @Test
    void deleteContest_shouldSucceed() {
        Contest contest = new Contest();
        contest.setName("测试竞赛");
        contest.setContestType(CommonConstants.CONTEST_PERSONAL);
        contest.setRegisterStartTime(java.time.LocalDateTime.now().plusDays(1));
        contest.setRegisterEndTime(java.time.LocalDateTime.now().plusDays(30));
        contest.setContestTime(java.time.LocalDateTime.now().plusDays(60));
        Contest created = contestService.createContest(contest);
        Long contestId = created.getId();
        contestService.deleteContest(contestId);
        Contest deleted = contestService.getById(contestId);
        assertNull(deleted);
    }

    @Test
    void getPublishedContests_shouldNotReturnDrafts() {
        Contest contest = new Contest();
        contest.setName("草稿竞赛");
        contest.setContestType(CommonConstants.CONTEST_PERSONAL);
        contest.setRegisterStartTime(java.time.LocalDateTime.now().plusDays(1));
        contest.setRegisterEndTime(java.time.LocalDateTime.now().plusDays(30));
        contest.setContestTime(java.time.LocalDateTime.now().plusDays(60));
        contestService.createContest(contest);
        var page = contestService.pageContests(1, 10, null, null, 1, null, null);
        boolean hasDraft = page.getRecords().stream().anyMatch(c -> c.getName().equals("草稿竞赛"));
        assertFalse(hasDraft);
    }
}
