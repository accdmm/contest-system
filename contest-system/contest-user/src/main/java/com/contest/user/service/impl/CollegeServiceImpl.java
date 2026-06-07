package com.contest.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.contest.user.entity.CollegeDO;
import com.contest.user.mapper.CollegeMapper;
import com.contest.user.service.CollegeService;
import org.springframework.stereotype.Service;

@Service
/** 学院服务实现 */
public class CollegeServiceImpl extends ServiceImpl<CollegeMapper, CollegeDO> implements CollegeService {
}
