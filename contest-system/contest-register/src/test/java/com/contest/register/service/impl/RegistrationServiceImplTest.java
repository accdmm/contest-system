package com.contest.register.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.contest.common.constant.CommonConstants;
import com.contest.common.exception.BusinessException;
import com.contest.common.service.TeamValidator;
import com.contest.competition.entity.ContestDO;
import com.contest.competition.service.ContestService;
import com.contest.message.service.NotificationService;
import com.contest.register.entity.RegistrationDO;
import com.contest.register.mapper.RegistrationMapper;
import com.contest.register.service.AdminNotifyService;
import com.contest.user.entity.UserDO;
import com.contest.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceImplTest {

    @Mock private RegistrationMapper registrationMapper;
    @Mock private ContestService contestService;
    @Mock private NotificationService notificationService;
    @Mock private UserService userService;
    @Mock private TeamValidator teamValidator;
    @Mock private AdminNotifyService adminNotifyService;

    private RegistrationServiceImpl registrationService;

    @BeforeEach
    void setUp() {
        registrationService = new RegistrationServiceImpl(contestService, notificationService, userService, teamValidator, adminNotifyService);
        ReflectionTestUtils.setField(registrationService, "baseMapper", registrationMapper);
    }

    private ContestDO makeOpenContest(int contestType) {
        ContestDO c = new ContestDO();
        c.setId(1L);
        c.setName("测试竞赛");
        c.setStatus(CommonConstants.CONTEST_OPEN);
        c.setContestType(contestType);
        c.setCurrentCount(0);
        c.setMaxParticipants(100);
        return c;
    }

    @Test
    void registerPersonal_shouldThrowWhenContestNotFound() {
        when(contestService.getById(1L)).thenReturn(null);

        assertThrows(BusinessException.class, () -> registrationService.registerPersonal(1L, 1L, ""));
    }

    @Test
    void registerPersonal_shouldThrowWhenContestNotOpen() {
        ContestDO c = makeOpenContest(CommonConstants.CONTEST_PERSONAL);
        c.setStatus(CommonConstants.CONTEST_DRAFT);
        when(contestService.getById(1L)).thenReturn(c);

        assertThrows(BusinessException.class, () -> registrationService.registerPersonal(1L, 1L, ""));
    }

    @Test
    void registerPersonal_shouldThrowWhenContestIsTeamOnly() {
        when(contestService.getById(1L)).thenReturn(makeOpenContest(CommonConstants.CONTEST_TEAM));

        assertThrows(BusinessException.class, () -> registrationService.registerPersonal(1L, 1L, ""));
    }

    @Test
    void registerPersonal_shouldThrowWhenAlreadyRegistered() {
        ContestDO c = makeOpenContest(CommonConstants.CONTEST_PERSONAL);
        when(contestService.getById(1L)).thenReturn(c);
        when(registrationMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        assertThrows(BusinessException.class, () -> registrationService.registerPersonal(1L, 1L, ""));
    }

    @Test
    void registerPersonal_shouldThrowWhenMaxParticipantsReached() {
        ContestDO c = makeOpenContest(CommonConstants.CONTEST_PERSONAL);
        c.setMaxParticipants(1);
        when(contestService.getById(1L)).thenReturn(c);
        when(registrationMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        assertThrows(BusinessException.class, () -> registrationService.registerPersonal(1L, 1L, ""));
    }

    @Test
    void registerPersonal_shouldSucceed() {
        ContestDO c = makeOpenContest(CommonConstants.CONTEST_PERSONAL);
        when(contestService.getById(1L)).thenReturn(c);
        when(registrationMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        UserDO user = new UserDO();
        user.setName("张三");
        when(userService.getById(1L)).thenReturn(user);
        when(registrationMapper.insert(any(RegistrationDO.class))).thenReturn(1);

        RegistrationDO result = registrationService.registerPersonal(1L, 1L, "参赛");

        assertNotNull(result);
        assertEquals(1L, result.getContestId());
        assertEquals(1L, result.getUserId());
        assertEquals(CommonConstants.REG_PERSONAL, result.getRegType());
        assertEquals(CommonConstants.REG_PENDING, result.getStatus());
        assertEquals("参赛", result.getRemark());
    }

    @Test
    void registerTeam_shouldThrowWhenContestNotOpen() {
        ContestDO c = makeOpenContest(CommonConstants.CONTEST_TEAM);
        c.setStatus(CommonConstants.CONTEST_CLOSED);
        when(contestService.getById(1L)).thenReturn(c);

        assertThrows(BusinessException.class, () -> registrationService.registerTeam(1L, 1L, 1L));
    }

    @Test
    void registerTeam_shouldThrowWhenContestIsPersonalOnly() {
        when(contestService.getById(1L)).thenReturn(makeOpenContest(CommonConstants.CONTEST_PERSONAL));

        assertThrows(BusinessException.class, () -> registrationService.registerTeam(1L, 1L, 1L));
    }

    @Test
    void registerTeam_shouldSucceed() {
        ContestDO c = makeOpenContest(CommonConstants.CONTEST_TEAM);
        when(contestService.getById(1L)).thenReturn(c);
        when(registrationMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        UserDO user = new UserDO();
        user.setName("张三");
        when(userService.getById(1L)).thenReturn(user);
        when(registrationMapper.insert(any(RegistrationDO.class))).thenReturn(1);

        RegistrationDO result = registrationService.registerTeam(1L, 1L, 1L);

        assertNotNull(result);
        assertEquals(CommonConstants.REG_TEAM, result.getRegType());
        assertEquals(1L, result.getTeamId());
    }

    @Test
    void cancelRegistration_shouldThrowWhenNotFound() {
        when(registrationMapper.selectById(1L)).thenReturn(null);

        assertThrows(BusinessException.class, () -> registrationService.cancelRegistration(1L, 1L));
    }

    @Test
    void cancelRegistration_shouldThrowWhenNotOwner() {
        RegistrationDO reg = new RegistrationDO();
        reg.setId(1L);
        reg.setUserId(1L);
        when(registrationMapper.selectById(1L)).thenReturn(reg);

        assertThrows(BusinessException.class, () -> registrationService.cancelRegistration(1L, 2L));
    }

    @Test
    void cancelRegistration_shouldNotTouchTeamStatus() {
        RegistrationDO reg = new RegistrationDO();
        reg.setId(1L);
        reg.setUserId(1L);
        reg.setStatus(CommonConstants.REG_APPROVED);
        reg.setContestId(1L);
        reg.setTeamId(1L);
        when(registrationMapper.selectById(1L)).thenReturn(reg);
        ContestDO c = makeOpenContest(CommonConstants.CONTEST_TEAM);
        c.setCurrentCount(1);
        when(contestService.getById(1L)).thenReturn(c);
        UserDO user = new UserDO();
        user.setName("张三");
        when(userService.getById(1L)).thenReturn(user);

        registrationService.cancelRegistration(1L, 1L);

        assertEquals(CommonConstants.REG_CANCELLED, reg.getStatus());
        assertEquals(0, c.getCurrentCount());
        verify(registrationMapper).updateById(reg);
        verify(contestService).updateById(c);
    }

    @Test
    void cancelRegistration_shouldNotDecrementWhenWasPending() {
        RegistrationDO reg = new RegistrationDO();
        reg.setId(1L);
        reg.setUserId(1L);
        reg.setStatus(CommonConstants.REG_PENDING);
        reg.setContestId(1L);
        when(registrationMapper.selectById(1L)).thenReturn(reg);
        ContestDO c = makeOpenContest(CommonConstants.CONTEST_PERSONAL);
        when(contestService.getById(1L)).thenReturn(c);
        UserDO user = new UserDO();
        user.setName("张三");
        when(userService.getById(1L)).thenReturn(user);

        registrationService.cancelRegistration(1L, 1L);

        assertEquals(CommonConstants.REG_CANCELLED, reg.getStatus());
        verify(contestService, never()).updateById(any());
    }

    @Test
    void approveRegistration_shouldThrowWhenNotFound() {
        when(registrationMapper.selectById(1L)).thenReturn(null);

        assertThrows(BusinessException.class, () -> registrationService.approveRegistration(1L));
    }

    @Test
    void approveRegistration_shouldIncrementCount() {
        RegistrationDO reg = new RegistrationDO();
        reg.setId(1L);
        reg.setUserId(1L);
        reg.setContestId(1L);
        reg.setStatus(CommonConstants.REG_PENDING);
        when(registrationMapper.selectById(1L)).thenReturn(reg);
        ContestDO c = makeOpenContest(CommonConstants.CONTEST_PERSONAL);
        c.setCurrentCount(5);
        when(contestService.getById(1L)).thenReturn(c);

        registrationService.approveRegistration(1L);

        assertEquals(CommonConstants.REG_APPROVED, reg.getStatus());
        assertEquals(6, c.getCurrentCount());
        verify(registrationMapper).updateById(reg);
        verify(contestService).updateById(c);
    }

    @Test
    void approveRegistration_shouldHandleNullCurrentCount() {
        RegistrationDO reg = new RegistrationDO();
        reg.setId(1L);
        reg.setUserId(1L);
        reg.setContestId(1L);
        reg.setStatus(CommonConstants.REG_PENDING);
        when(registrationMapper.selectById(1L)).thenReturn(reg);
        ContestDO c = makeOpenContest(CommonConstants.CONTEST_PERSONAL);
        c.setCurrentCount(null);
        when(contestService.getById(1L)).thenReturn(c);

        registrationService.approveRegistration(1L);

        assertEquals(1, c.getCurrentCount());
    }

    @Test
    void rejectRegistration_shouldThrowWhenReasonTooShort() {
        assertThrows(BusinessException.class, () -> registrationService.rejectRegistration(1L, "否"));
    }

    @Test
    void rejectRegistration_shouldThrowWhenNullReason() {
        assertThrows(BusinessException.class, () -> registrationService.rejectRegistration(1L, null));
    }

    @Test
    void rejectRegistration_shouldSucceed() {
        RegistrationDO reg = new RegistrationDO();
        reg.setId(1L);
        reg.setUserId(1L);
        reg.setContestId(1L);
        reg.setStatus(CommonConstants.REG_PENDING);
        when(registrationMapper.selectById(1L)).thenReturn(reg);

        registrationService.rejectRegistration(1L, "材料不全，请补充后重新提交");

        assertEquals(CommonConstants.REG_REJECTED, reg.getStatus());
        assertEquals("材料不全，请补充后重新提交", reg.getReviewReason());
        verify(registrationMapper).updateById(reg);
    }

    @Test
    void rejectRegistration_shouldDecrementCountWhenWasApproved() {
        RegistrationDO reg = new RegistrationDO();
        reg.setId(1L);
        reg.setUserId(1L);
        reg.setContestId(1L);
        reg.setStatus(CommonConstants.REG_APPROVED);
        when(registrationMapper.selectById(1L)).thenReturn(reg);
        ContestDO c = makeOpenContest(CommonConstants.CONTEST_PERSONAL);
        c.setCurrentCount(5);
        when(contestService.getById(1L)).thenReturn(c);

        registrationService.rejectRegistration(1L, "材料不全，请补充后重新提交");

        assertEquals(4, c.getCurrentCount());
        verify(contestService).updateById(c);
    }

    @Test
    void rejectRegistration_shouldNotDecrementWhenWasPending() {
        RegistrationDO reg = new RegistrationDO();
        reg.setId(1L);
        reg.setUserId(1L);
        reg.setContestId(1L);
        reg.setStatus(CommonConstants.REG_PENDING);
        when(registrationMapper.selectById(1L)).thenReturn(reg);

        registrationService.rejectRegistration(1L, "材料不全，请补充后重新提交");

        verify(contestService, never()).updateById(any());
    }

    @Test
    void pageByUser_shouldReturnPagedResult() {
        RegistrationDO reg = new RegistrationDO();
        reg.setId(1L);
        reg.setUserId(1L);
        reg.setContestId(1L);
        when(registrationMapper.selectPage(any(), any(LambdaQueryWrapper.class))).thenReturn(
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<RegistrationDO>() {{
                    setRecords(java.util.List.of(reg));
                    setTotal(1);
                }}
        );
        ContestDO c = makeOpenContest(CommonConstants.CONTEST_PERSONAL);
        when(contestService.listByIds(anyList())).thenReturn(java.util.List.of(c));

        var page = registrationService.pageByUser(1L, 1, 10);

        assertEquals(1, page.getTotal());
        assertEquals(1, page.getRecords().size());
        assertEquals(c.getName(), page.getRecords().get(0).getContestName());
    }
}
