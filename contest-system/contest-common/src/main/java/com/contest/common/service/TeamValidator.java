package com.contest.common.service;

public interface TeamValidator {
    void validateForRegistration(Long teamId);
    void validateTeamLeader(Long teamId, Long userId);
}
