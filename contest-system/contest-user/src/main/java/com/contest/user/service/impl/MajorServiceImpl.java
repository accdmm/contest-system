package com.contest.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.contest.user.entity.Major;
import com.contest.user.mapper.MajorMapper;
import com.contest.user.service.MajorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;

@Slf4j
@Service
public class MajorServiceImpl extends ServiceImpl<MajorMapper, Major> implements MajorService {

    @Override
    public List<Major> getByCollegeId(Integer collegeId) {
        log.info("query majors by collegeId={}", collegeId);
        return list(new LambdaQueryWrapper<Major>()
                .eq(Major::getCollegeId, collegeId)
                .orderByAsc(Major::getId));
    }
}
