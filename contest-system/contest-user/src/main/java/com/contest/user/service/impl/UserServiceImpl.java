package com.contest.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.contest.common.constant.CommonConstants;
import com.contest.common.exception.BusinessException;
import com.contest.user.entity.User;
import com.contest.user.mapper.UserMapper;
import com.contest.user.service.UserService;
import org.springframework.stereotype.Service;
import cn.hutool.crypto.digest.DigestUtil;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public User login(String username, String password) {
        User user = getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getStatus() == CommonConstants.STATUS_FROZEN) {
            throw new BusinessException("账号已被冻结");
        }
        if (!DigestUtil.bcryptCheck(password, user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        return user;
    }

    @Override
    public User register(User user, String rawPassword) {
        long count = count(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, user.getUsername()));
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }
        user.setPassword(DigestUtil.bcrypt(rawPassword));
        user.setRole(CommonConstants.ROLE_STUDENT);
        user.setStatus(CommonConstants.STATUS_NORMAL);
        save(user);
        return user;
    }

    @Override
    public void updateProfile(Long userId, User user) {
        User existing = getById(userId);
        if (existing == null) {
            throw new BusinessException("用户不存在");
        }
        user.setId(userId);
        user.setPassword(null);
        user.setUsername(null);
        user.setRole(null);
        user.setStatus(null);
        updateById(user);
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!DigestUtil.bcryptCheck(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        user.setPassword(DigestUtil.bcrypt(newPassword));
        updateById(user);
    }

    @Override
    public void freezeUser(Long userId) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setStatus(CommonConstants.STATUS_FROZEN);
        updateById(user);
    }

    @Override
    public void unfreezeUser(Long userId) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setStatus(CommonConstants.STATUS_NORMAL);
        updateById(user);
    }
}
