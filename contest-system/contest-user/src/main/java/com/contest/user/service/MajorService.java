package com.contest.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.contest.user.entity.MajorDO;
import java.util.List;

public interface MajorService extends IService<MajorDO> {
    List<MajorDO> getByCollegeId(Integer collegeId);
}
