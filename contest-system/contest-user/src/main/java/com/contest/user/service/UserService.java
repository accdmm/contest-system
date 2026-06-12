package com.contest.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.contest.user.entity.User;
import java.util.List;

/**
 * 用户服务接口
 *
 * 定义用户注册、登录、资料管理、冻结/解冻等核心业务方法。
 * 安全性相关：密码 BCrypt 加密存储、密码强度校验、账号冻结状态检查。
 */
public interface UserService extends IService<User> {

    /**
     * 用户登录
     *
     * 校验流程：学号存在性 → 账号状态（是否冻结）→ 密码 BCrypt 匹配。
     * 密码错误时返回统一提示"用户名或密码错误"，不区分具体是哪项错误。
     *
     * @param username 学号/管理员登录名
     * @param password 原始密码（非 BCrypt）
     * @return 登录成功的用户对象（不含密码）
     */
    User login(String username, String password);

    /**
     * 用户注册
     *
     * 校验流程：学号唯一性 → 邮箱唯一性 → 手机号唯一性 → 密码强度校验 → BCrypt 加密。
     *
     * @param user        用户信息（含学号、姓名、邮箱、手机号等）
     * @param rawPassword 原始密码（8-20位，需含字母和数字）
     * @return 注册成功的用户对象
     */
    User register(User user, String rawPassword);

    /**
     * 修改用户资料
     *
     * @param userId 目标用户 ID
     * @param user   新的资料（姓名、邮箱、手机号、头像等）
     */
    void updateProfile(Long userId, User user);

    /**
     * 修改密码
     *
     * 需校验旧密码正确性（BCrypt 匹配），新密码需满足强度要求。
     *
     * @param userId      用户 ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    void changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 冻结用户：将账号状态置为冻结，登录时被拦截
     *
     * @param userId 用户 ID
     */
    void freezeUser(Long userId);

    /**
     * 解冻用户：恢复账号正常状态
     *
     * @param userId 用户 ID
     */
    void unfreezeUser(Long userId);

    /**
     * 分页查询用户
     *
     * @param keyword 搜索关键词（按学号或姓名模糊匹配）
     * @param page    页码
     * @param size    每页大小
     * @return 用户分页数据
     */
    IPage<User> pageUsers(String keyword, Integer page, Integer size);

    /**
     * 获取教师列表（用于创建团队时选择指导教师）
     *
     * @return 教师用户列表
     */
    List<User> listTeachers();

    /**
     * 管理员创建用户（可指定角色，非管理员不能创建管理员账号）
     *
     * @param user        用户信息
     * @param rawPassword 原始密码
     * @return 创建成功的用户对象
     */
    User adminCreateUser(User user, String rawPassword);
}
