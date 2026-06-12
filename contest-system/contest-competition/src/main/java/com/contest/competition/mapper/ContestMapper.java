package com.contest.competition.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.contest.competition.entity.Contest;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/** 竞赛数据访问接口 */
public interface ContestMapper extends BaseMapper<Contest> {

    /** 查询报名人数最多的竞赛（Top N） */
    @Select("SELECT id, name, current_count FROM contest ORDER BY current_count DESC LIMIT #{limit}")
    List<Map<String, Object>> selectTopContests(@Param("limit") int limit);
}
