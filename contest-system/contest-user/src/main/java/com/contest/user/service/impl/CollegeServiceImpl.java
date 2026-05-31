package com.contest.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.contest.user.entity.College;
import com.contest.user.mapper.CollegeMapper;
import com.contest.user.service.CollegeService;
import org.springframework.stereotype.Service;

@Service
public class CollegeServiceImpl extends ServiceImpl<CollegeMapper, College> implements CollegeService {
}
