package com.contest.team.service.impl;

import com.contest.common.constant.CommonConstants;
import com.contest.common.exception.BusinessException;
import com.contest.common.service.TeamValidator;
import com.contest.team.entity.Team;
import com.contest.team.mapper.TeamMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
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
            log.warn("team not found for registration: teamId={}", teamId);
            throw new BusinessException("团队不存在或已解散");
        }
        if (team.getStatus() != CommonConstants.TEAM_APPROVED) {
            log.warn("team not approved for registration: teamId={}, status={}", teamId, team.getStatus());
            throw new BusinessException("团队审核未通过，无法报名竞赛");
        }
    }

    @Override
    public void validateTeamLeader(Long teamId, Long userId) {
        Team team = teamMapper.selectById(teamId);
        if (team == null) {
            log.warn("team not found for leader validation: teamId={}", teamId);
            throw new BusinessException("团队不存在或已解散");
        }
        if (!team.getLeaderId().equals(userId)) {
            log.warn("user is not team leader: userId={}, teamId={}, leaderId={}", userId, teamId, team.getLeaderId());
            throw new BusinessException("仅队长可进行此操作");
        }
    }
}
