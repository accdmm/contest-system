package com.contest.register.test;

import com.contest.common.constant.CommonConstants;
import com.contest.common.exception.BusinessException;
import com.contest.common.service.TeamValidator;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class TeamValidatorStub implements TeamValidator {

    private final JdbcTemplate jdbc;

    public TeamValidatorStub(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void validateForRegistration(Long teamId) {
        try {
            Integer status = jdbc.queryForObject(
                    "SELECT status FROM team WHERE id = ? AND is_delete = 0",
                    Integer.class, teamId);
            if (status == null || status != CommonConstants.TEAM_APPROVED) {
                throw new BusinessException("团队审核未通过，无法报名竞赛");
            }
        } catch (EmptyResultDataAccessException e) {
            throw new BusinessException("团队不存在或已解散");
        }
    }

    @Override
    public void validateTeamLeader(Long teamId, Long userId) {
        try {
            Long leaderId = jdbc.queryForObject(
                    "SELECT leader_id FROM team WHERE id = ? AND is_delete = 0",
                    Long.class, teamId);
            if (!leaderId.equals(userId)) {
                throw new BusinessException("仅队长可进行此操作");
            }
        } catch (EmptyResultDataAccessException e) {
            throw new BusinessException("团队不存在或已解散");
        }
    }
}
