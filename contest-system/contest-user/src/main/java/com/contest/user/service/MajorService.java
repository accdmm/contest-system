package com.contest.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.contest.user.entity.Major;
import java.util.List;

public interface MajorService extends IService<Major> {
    List<Major> getByCollegeId(Integer collegeId);
}
