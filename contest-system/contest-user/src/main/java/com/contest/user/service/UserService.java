package com.contest.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.contest.user.entity.User;
import java.util.List;

public interface UserService extends IService<User> {

    User login(String username, String password);

    User register(User user, String rawPassword);

    void updateProfile(Long userId, User user);

    void changePassword(Long userId, String oldPassword, String newPassword);

    void freezeUser(Long userId);

    void unfreezeUser(Long userId);

    IPage<User> pageUsers(String keyword, Integer page, Integer size);

    List<User> listTeachers();

    User adminCreateUser(User user, String rawPassword);
}
