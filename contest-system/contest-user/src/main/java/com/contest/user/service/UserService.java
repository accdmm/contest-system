package com.contest.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.contest.user.entity.UserDO;
import java.util.List;

/** 用户服务接口 */
public interface UserService extends IService<UserDO> {

    /** 用户登录 \n @param username 用户名 \n @param password 密码 \n @return 登录成功的用户对象 */
    UserDO login(String username, String password);

    /** 用户注册 \n @param user 用户信息 \n @param rawPassword 原始密码 \n @return 注册成功的用户对象 */
    UserDO register(UserDO user, String rawPassword);

    /** 修改用户资料 \n @param userId 用户ID \n @param user 新的用户资料 */
    void updateProfile(Long userId, UserDO user);

    /** 修改密码 \n @param userId 用户ID \n @param oldPassword 原密码 \n @param newPassword 新密码 */
    void changePassword(Long userId, String oldPassword, String newPassword);

    /** 冻结用户 \n @param userId 用户ID */
    void freezeUser(Long userId);

    /** 解冻用户 \n @param userId 用户ID */
    void unfreezeUser(Long userId);

    /** 分页查询用户 \n @param keyword 搜索关键词 \n @param page 页码 \n @param size 每页大小 \n @return 用户分页数据 */
    IPage<UserDO> pageUsers(String keyword, Integer page, Integer size);

    /** 获取教师列表 \n @return 教师用户列表 */
    List<UserDO> listTeachers();

    /** 管理员创建用户 \n @param user 用户信息 \n @param rawPassword 原始密码 \n @return 创建成功的用户对象 */
    UserDO adminCreateUser(UserDO user, String rawPassword);
}
