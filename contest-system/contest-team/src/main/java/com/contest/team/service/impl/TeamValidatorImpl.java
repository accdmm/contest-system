package com.contest.team.service.impl;

import com.contest.common.constant.CommonConstants;
import com.contest.common.exception.BusinessException;
import com.contest.common.service.TeamValidator;
import com.contest.team.entity.Team;
import com.contest.team.mapper.TeamMapper;
import org.springframework.stereotype.Component;

@Component
public class TeamValidatorImpl implements TeamValidator {

    private final TeamMapper teamMapper;

    public TeamValidatorImpl(TeamMapper teamMapper) {
        this.teamMapper = teamMapper;
    }

    @Override
    public void validateForRegistration(Long teamId) {
        Team team = teamMapper.selectById(teamId);
        if (team == null) {
            throw new BusinessException("团队不存在或已解散");
        }
        if (team.getStatus() != CommonConstants.TEAM_APPROVED) {
            throw new BusinessException("团队审核未通过，无法报名竞赛");
        }
    }
}
