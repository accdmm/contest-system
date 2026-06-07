package com.contest.register.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.contest.register.entity.RegistrationDO;

public interface RegistrationService extends IService<RegistrationDO> {

    RegistrationDO registerPersonal(Long userId, Long contestId, String remark);

    RegistrationDO registerTeam(Long userId, Long contestId, Long teamId);

    void approveRegistration(Long id);

    void rejectRegistration(Long id, String reason);

    void cancelRegistration(Long id, Long userId);

    IPage<RegistrationDO> pageByUser(Long userId, Integer page, Integer size);

    IPage<RegistrationDO> pageByContest(Long contestId, Integer page, Integer size, Integer status);

    IPage<RegistrationDO> pageAll(Long contestId, Integer status, Integer page, Integer size);
}
