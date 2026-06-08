package com.contest.common.service;

/**
 * 团队校验服务接口
 *
 * <p>提供报名前团队有效性校验能力。定义在 contest-common 模块中，
 * 实现由 contest-team 模块提供，通过依赖注入解耦。
 * RegistrationServiceImpl 在团队报名时调用此接口校验团队状态。
 */
public interface TeamValidator {

    /**
     * 校验团队是否可报名
     *
     * <p>检查团队是否存在、是否已解散、是否已通过审核等。
     *
     * @param teamId 团队 ID
     */
    void validateForRegistration(Long teamId);

    /**
     * 校验用户是否为团队队长
     *
     * @param teamId 团队 ID
     * @param userId 用户 ID
     */
    void validateTeamLeader(Long teamId, Long userId);
}
