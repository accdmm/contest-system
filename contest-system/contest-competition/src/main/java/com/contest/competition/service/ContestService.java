package com.contest.competition.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.contest.competition.entity.Contest;

import java.util.List;

public interface ContestService extends IService<Contest> {

    Contest createContest(Contest contest);

    Contest updateContest(Contest contest);

    void publishContest(Long id);

    void unpublishContest(Long id);

    void deleteContest(Long id);

    IPage<Contest> pageContests(Integer page, Integer size, String keyword, String category, Integer status, Integer contestType, String sortBy);

    IPage<Contest> pageContests(Integer page, Integer size, String keyword, String category, Integer status, String sortBy);

    List<Contest> listHotContests(int limit);

    List<Contest> listLatestContests(int limit);
}
