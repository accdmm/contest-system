package com.contest.competition.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.contest.competition.entity.ContestDO;

import java.util.List;

public interface ContestService extends IService<ContestDO> {

    ContestDO createContest(ContestDO contest);

    ContestDO updateContest(ContestDO contest);

    void publishContest(Long id);

    void unpublishContest(Long id);

    void deleteContest(Long id);

    IPage<ContestDO> pageContests(Integer page, Integer size, String keyword, String category, Integer status, Integer contestType, String sortBy);

    IPage<ContestDO> pageContests(Integer page, Integer size, String keyword, String category, Integer status, String sortBy);

    List<ContestDO> listHotContests(int limit);

    List<ContestDO> listLatestContests(int limit);
}
