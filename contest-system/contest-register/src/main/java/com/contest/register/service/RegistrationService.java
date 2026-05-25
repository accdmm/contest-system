package com.contest.register.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.contest.register.entity.Registration;

public interface RegistrationService extends IService<Registration> {

    Registration registerPersonal(Long userId, Long contestId, String remark);

    Registration registerTeam(Long userId, Long contestId, Long teamId);

    void approveRegistration(Long id);

    void rejectRegistration(Long id, String reason);

    void cancelRegistration(Long id, Long userId);

    IPage<Registration> pageByUser(Long userId, Integer page, Integer size);

    IPage<Registration> pageByContest(Long contestId, Integer page, Integer size, Integer status);
}
