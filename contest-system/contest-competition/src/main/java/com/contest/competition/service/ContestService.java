package com.contest.competition.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.contest.competition.entity.ContestDO;

import java.util.List;

/** 竞赛服务接口 */
public interface ContestService extends IService<ContestDO> {

    /** 创建竞赛
     * @param contest 竞赛实体
     * @return 创建后的竞赛 */
    ContestDO createContest(ContestDO contest);

    /** 修改竞赛
     * @param contest 竞赛实体
     * @return 修改后的竞赛 */
    ContestDO updateContest(ContestDO contest);

    /** 上架竞赛
     * @param id 竞赛ID */
    void publishContest(Long id);

    /** 下架竞赛
     * @param id 竞赛ID */
    void unpublishContest(Long id);

    /** 删除竞赛
     * @param id 竞赛ID */
    void deleteContest(Long id);

    /** 分页查询竞赛列表
     * @param page 页码
     * @param size 每页条数
     * @param keyword 关键词
     * @param category 竞赛类别
     * @param status 状态
     * @param contestType 竞赛类型
     * @param sortBy 排序字段
     * @return 分页结果 */
    IPage<ContestDO> pageContests(Integer page, Integer size, String keyword, String category, Integer status, Integer contestType, String sortBy);

    /** 分页查询竞赛列表（不含竞赛类型筛选）
     * @param page 页码
     * @param size 每页条数
     * @param keyword 关键词
     * @param category 竞赛类别
     * @param status 状态
     * @param sortBy 排序字段
     * @return 分页结果 */
    IPage<ContestDO> pageContests(Integer page, Integer size, String keyword, String category, Integer status, String sortBy);

    /** 获取热门竞赛列表
     * @param limit 返回条数
     * @return 热门竞赛列表 */
    List<ContestDO> listHotContests(int limit);

    /** 获取最新竞赛列表
     * @param limit 返回条数
     * @return 最新竞赛列表 */
    List<ContestDO> listLatestContests(int limit);

    /** 管理员分页查询所有竞赛（包含草稿、已发布、已截止，不按状态过滤）
     * @param page 页码
     * @param size 每页条数
     * @param keyword 关键词
     * @param category 竞赛类别
     * @param status 状态（可选，传 null 显示全部）
     * @return 分页结果 */
    IPage<ContestDO> pageAdminContests(Integer page, Integer size, String keyword, String category, Integer status);
}
