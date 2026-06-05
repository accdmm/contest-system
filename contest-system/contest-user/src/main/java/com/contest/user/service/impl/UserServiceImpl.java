package com.contest.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.contest.common.constant.CommonConstants;
import com.contest.common.exception.BusinessException;
import com.contest.user.entity.College;
import com.contest.user.entity.Major;
import com.contest.user.entity.User;
import com.contest.user.mapper.CollegeMapper;
import com.contest.user.mapper.MajorMapper;
import com.contest.user.mapper.UserMapper;
import com.contest.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import cn.hutool.crypto.digest.DigestUtil;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final CollegeMapper collegeMapper;
    private final MajorMapper majorMapper;

    public UserServiceImpl(CollegeMapper collegeMapper, MajorMapper majorMapper) {
        this.collegeMapper = collegeMapper;
        this.majorMapper = majorMapper;
    }

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

    private void validatePassword(String password) {
        if (password == null || password.length() < 8 || password.length() > 20) {
            throw new BusinessException("密码长度需为8-20位");
        }
        if (!Pattern.compile("[a-zA-Z]").matcher(password).find()
                || !Pattern.compile("[0-9]").matcher(password).find()) {
            throw new BusinessException("密码需同时包含字母和数字");
        }
    }

    @Override
    public User register(User user, String rawPassword) {
        validatePassword(rawPassword);
        long count = count(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, user.getUsername()));
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }
        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            long emailCount = count(new LambdaQueryWrapper<User>()
                    .eq(User::getEmail, user.getEmail()));
            if (emailCount > 0) {
                throw new BusinessException("邮箱已被其他用户使用");
            }
        }
        if (user.getPhone() != null && !user.getPhone().isBlank()) {
            long phoneCount = count(new LambdaQueryWrapper<User>()
                    .eq(User::getPhone, user.getPhone()));
            if (phoneCount > 0) {
                throw new BusinessException("手机号已被其他用户使用");
            }
        }
        if (user.getCollegeId() != null) {
            College college = collegeMapper.selectById(user.getCollegeId());
            if (college != null) {
                user.setCollege(college.getName());
            }
        }
        if (user.getMajorId() != null) {
            Major major = majorMapper.selectById(user.getMajorId());
            if (major != null) {
                user.setMajor(major.getName());
            }
        }
        user.setPassword(DigestUtil.bcrypt(rawPassword));
        user.setRole(CommonConstants.ROLE_STUDENT);
        user.setStatus(CommonConstants.STATUS_NORMAL);
        save(user);
        return user;
    }

    @Override
    public User adminCreateUser(User user, String rawPassword) {
        validatePassword(rawPassword);
        long count = count(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, user.getUsername()));
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }
        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            long emailCount = count(new LambdaQueryWrapper<User>()
                    .eq(User::getEmail, user.getEmail()));
            if (emailCount > 0) {
                throw new BusinessException("邮箱已被其他用户使用");
            }
        }
        if (user.getPhone() != null && !user.getPhone().isBlank()) {
            long phoneCount = count(new LambdaQueryWrapper<User>()
                    .eq(User::getPhone, user.getPhone()));
            if (phoneCount > 0) {
                throw new BusinessException("手机号已被其他用户使用");
            }
        }
        if (user.getCollegeId() != null) {
            College college = collegeMapper.selectById(user.getCollegeId());
            if (college != null) {
                user.setCollege(college.getName());
            }
        }
        if (user.getMajorId() != null) {
            Major major = majorMapper.selectById(user.getMajorId());
            if (major != null) {
                user.setMajor(major.getName());
            }
        }
        if (user.getRole() == null ||
                (user.getRole() != CommonConstants.ROLE_STUDENT
                && user.getRole() != CommonConstants.ROLE_TEACHER
                && user.getRole() != CommonConstants.ROLE_ADMIN)) {
            throw new BusinessException("无效的角色值");
        }
        user.setPassword(DigestUtil.bcrypt(rawPassword));
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
        // 防止前端提交空字符串覆盖已有头像
        if (user.getAvatarUrl() != null && user.getAvatarUrl().isBlank()) {
            user.setAvatarUrl(null);
        }
        // 同步冗余字段：collegeId → college（文字），仅当查找成功时才更新
        if (user.getCollegeId() != null) {
            College college = collegeMapper.selectById(user.getCollegeId());
            if (college != null) {
                user.setCollege(college.getName());
            } else {
                // 若 collegeId 无效则跳过该字段的更新
                user.setCollegeId(null);
            }
        }
        if (user.getMajorId() != null) {
            Major major = majorMapper.selectById(user.getMajorId());
            if (major != null) {
                user.setMajor(major.getName());
            } else {
                user.setMajorId(null);
            }
        }
        user.setId(userId);
        user.setPassword(null);
        user.setUsername(null);
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
        validatePassword(newPassword);
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

    @Override
    public IPage<User> pageUsers(String keyword, Integer page, Integer size) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(User::getUsername, keyword)
                   .or()
                   .like(User::getName, keyword)
                   .or()
                   .like(User::getCollege, keyword)
                   .or()
                   .like(User::getMajor, keyword);
        }
        wrapper.orderByDesc(User::getCreateTime);
        IPage<User> result = page(new Page<>(page, size), wrapper);
        result.getRecords().forEach(u -> u.setPassword(null));
        return result;
    }

    @Override
    public List<User> listTeachers() {
        List<User> teachers = list(new LambdaQueryWrapper<User>()
                .eq(User::getRole, CommonConstants.ROLE_TEACHER)
                .eq(User::getStatus, CommonConstants.STATUS_NORMAL));
        teachers.forEach(t -> t.setPassword(null));
        return teachers;
    }
}
