package com.contest.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.contest.user.entity.Major;
import com.contest.user.mapper.MajorMapper;
import com.contest.user.service.MajorService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
/** 专业服务实现 */
public class MajorServiceImpl extends ServiceImpl<MajorMapper, Major> implements MajorService {

    /** 根据学院ID查询专业列表 */
    @Override
    public List<Major> getByCollegeId(Integer collegeId) {
        return list(new LambdaQueryWrapper<Major>()
                .eq(Major::getCollegeId, collegeId)
                .orderByAsc(Major::getId));
    }
}
