package com.contest.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.contest.user.entity.MajorDO;
import com.contest.user.mapper.MajorMapper;
import com.contest.user.service.MajorService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MajorServiceImpl extends ServiceImpl<MajorMapper, MajorDO> implements MajorService {

    @Override
    public List<MajorDO> getByCollegeId(Integer collegeId) {
        return list(new LambdaQueryWrapper<MajorDO>()
                .eq(MajorDO::getCollegeId, collegeId)
                .orderByAsc(MajorDO::getId));
    }
}
