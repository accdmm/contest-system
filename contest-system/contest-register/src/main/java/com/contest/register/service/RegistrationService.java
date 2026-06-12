package com.contest.register.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.contest.register.entity.Registration;

/** 报名服务接口 */
public interface RegistrationService extends IService<Registration> {

    /**
     * 个人赛报名
     * @param userId 用户ID
     * @param contestId 竞赛ID
     * @param remark 备注
     * @return 报名记录
     */
    Registration registerPersonal(Long userId, Long contestId, String remark);

    /**
     * 团队赛报名
     * @param userId 队长用户ID
     * @param contestId 竞赛ID
     * @param teamId 团队ID
     * @return 报名记录
     */
    Registration registerTeam(Long userId, Long contestId, Long teamId);

    /**
     * 审核通过报名
     * @param id 报名记录ID
     */
    void approveRegistration(Long id);

    /**
     * 驳回报名
     * @param id 报名记录ID
     * @param reason 驳回原因
     */
    void rejectRegistration(Long id, String reason);

    /**
     * 取消报名
     * @param id 报名记录ID
     * @param userId 操作用户ID
     */
    void cancelRegistration(Long id, Long userId);

    /**
     * 分页查询用户报名记录
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页条数
     * @return 分页结果
     */
    IPage<Registration> pageByUser(Long userId, Integer page, Integer size);

    /**
     * 分页查询竞赛的报名记录
     * @param contestId 竞赛ID
     * @param page 页码
     * @param size 每页条数
     * @param status 状态筛选
     * @return 分页结果
     */
    IPage<Registration> pageByContest(Long contestId, Integer page, Integer size, Integer status);

    /**
     * 分页查询全部报名记录
     * @param contestId 竞赛ID（可选）
     * @param status 状态（可选）
     * @param page 页码
     * @param size 每页条数
     * @return 分页结果
     */
    IPage<Registration> pageAll(Long contestId, Integer status, Integer page, Integer size);
}
