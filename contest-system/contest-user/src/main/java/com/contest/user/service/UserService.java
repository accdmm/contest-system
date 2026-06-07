package com.contest.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.contest.user.entity.UserDO;
import java.util.List;

public interface UserService extends IService<UserDO> {

    UserDO login(String username, String password);

    UserDO register(UserDO user, String rawPassword);

    void updateProfile(Long userId, UserDO user);

    void changePassword(Long userId, String oldPassword, String newPassword);

    void freezeUser(Long userId);

    void unfreezeUser(Long userId);

    IPage<UserDO> pageUsers(String keyword, Integer page, Integer size);

    List<UserDO> listTeachers();

    UserDO adminCreateUser(UserDO user, String rawPassword);
}
