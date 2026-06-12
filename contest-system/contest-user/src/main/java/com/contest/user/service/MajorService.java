package com.contest.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.contest.user.entity.Major;
import java.util.List;

/** 专业服务接口 */
public interface MajorService extends IService<Major> {
    /** 根据学院ID获取专业列表 \n @param collegeId 学院ID \n @return 专业列表 */
    List<Major> getByCollegeId(Integer collegeId);
}
