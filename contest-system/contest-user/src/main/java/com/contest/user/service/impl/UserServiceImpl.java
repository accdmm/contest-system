package com.contest.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.contest.common.constant.CommonConstants;
import com.contest.common.exception.BusinessException;
import com.contest.user.entity.CollegeDO;
import com.contest.user.entity.MajorDO;
import com.contest.user.entity.UserDO;
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
/**
 * 用户服务实现
 *
 * 处理用户注册、登录、密码管理、账号冻结等业务逻辑。
 * 注册时做唯一性校验和 BCrypt 加密，登录时检查账号状态。
 */
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {

    private final CollegeMapper collegeMapper;
    private final MajorMapper majorMapper;

    public UserServiceImpl(CollegeMapper collegeMapper, MajorMapper majorMapper) {
        this.collegeMapper = collegeMapper;
        this.majorMapper = majorMapper;
    }

    /** 用户登录：校验用户名密码、检查账号状态 */
    @Override
    public UserDO login(String username, String password) {
        UserDO user = getOne(new LambdaQueryWrapper<UserDO>()
                .eq(UserDO::getUsername, username));
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

    /** 校验密码强度：8-20位，需包含字母和数字 */
    private void validatePassword(String password) {
        if (password == null || password.length() < 8 || password.length() > 20) {
            throw new BusinessException("密码长度需为8-20位");
        }
        if (!Pattern.compile("[a-zA-Z]").matcher(password).find()
                || !Pattern.compile("[0-9]").matcher(password).find()) {
            throw new BusinessException("密码需同时包含字母和数字");
        }
    }

    /** 用户注册：校验唯一性、BCrypt加密、默认角色为学生 */
    @Override
    public UserDO register(UserDO user, String rawPassword) {
        validatePassword(rawPassword);
        long count = count(new LambdaQueryWrapper<UserDO>()
                .eq(UserDO::getUsername, user.getUsername()));
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }
        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            long emailCount = count(new LambdaQueryWrapper<UserDO>()
                    .eq(UserDO::getEmail, user.getEmail()));
            if (emailCount > 0) {
                throw new BusinessException("邮箱已被其他用户使用");
            }
        }
        if (user.getPhone() != null && !user.getPhone().isBlank()) {
            long phoneCount = count(new LambdaQueryWrapper<UserDO>()
                    .eq(UserDO::getPhone, user.getPhone()));
            if (phoneCount > 0) {
                throw new BusinessException("手机号已被其他用户使用");
            }
        }
        if (user.getCollegeId() != null) {
            CollegeDO college = collegeMapper.selectById(user.getCollegeId());
            if (college != null) {
                user.setCollege(college.getName());
            }
        }
        if (user.getMajorId() != null) {
            MajorDO major = majorMapper.selectById(user.getMajorId());
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

    /** 管理员创建用户：校验唯一性和角色值，BCrypt加密 */
    @Override
    public UserDO adminCreateUser(UserDO user, String rawPassword) {
        validatePassword(rawPassword);
        long count = count(new LambdaQueryWrapper<UserDO>()
                .eq(UserDO::getUsername, user.getUsername()));
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }
        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            long emailCount = count(new LambdaQueryWrapper<UserDO>()
                    .eq(UserDO::getEmail, user.getEmail()));
            if (emailCount > 0) {
                throw new BusinessException("邮箱已被其他用户使用");
            }
        }
        if (user.getPhone() != null && !user.getPhone().isBlank()) {
            long phoneCount = count(new LambdaQueryWrapper<UserDO>()
                    .eq(UserDO::getPhone, user.getPhone()));
            if (phoneCount > 0) {
                throw new BusinessException("手机号已被其他用户使用");
            }
        }
        if (user.getCollegeId() != null) {
            CollegeDO college = collegeMapper.selectById(user.getCollegeId());
            if (college != null) {
                user.setCollege(college.getName());
            }
        }
        if (user.getMajorId() != null) {
            MajorDO major = majorMapper.selectById(user.getMajorId());
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

    /** 修改用户资料：同步学院/专业冗余文字字段 */
    @Override
    public void updateProfile(Long userId, UserDO user) {
        UserDO existing = getById(userId);
        if (existing == null) {
            throw new BusinessException("用户不存在");
        }
        // 防止前端提交空字符串覆盖已有头像
        if (user.getAvatarUrl() != null && user.getAvatarUrl().isBlank()) {
            user.setAvatarUrl(null);
        }
        // 同步冗余字段：collegeId → college（文字），仅当查找成功时才更新
        if (user.getCollegeId() != null) {
            CollegeDO college = collegeMapper.selectById(user.getCollegeId());
            if (college != null) {
                user.setCollege(college.getName());
            } else {
                // 若 collegeId 无效则跳过该字段的更新
                user.setCollegeId(null);
            }
        }
        if (user.getMajorId() != null) {
            MajorDO major = majorMapper.selectById(user.getMajorId());
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

    /** 修改密码：校验原密码后更新为新密码 */
    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        UserDO user = getById(userId);
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

    /** 冻结用户 */
    @Override
    public void freezeUser(Long userId) {
        UserDO user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setStatus(CommonConstants.STATUS_FROZEN);
        updateById(user);
    }

    /** 解冻用户 */
    @Override
    public void unfreezeUser(Long userId) {
        UserDO user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setStatus(CommonConstants.STATUS_NORMAL);
        updateById(user);
    }

    /** 分页查询用户：支持按用户名、姓名、学院、专业模糊搜索 */
    @Override
    public IPage<UserDO> pageUsers(String keyword, Integer page, Integer size) {
        LambdaQueryWrapper<UserDO> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(UserDO::getUsername, keyword)
                   .or()
                   .like(UserDO::getName, keyword)
                   .or()
                   .like(UserDO::getCollege, keyword)
                   .or()
                   .like(UserDO::getMajor, keyword);
        }
        wrapper.orderByDesc(UserDO::getCreateTime);
        IPage<UserDO> result = page(new Page<>(page, size), wrapper);
        result.getRecords().forEach(u -> u.setPassword(null));
        return result;
    }

    /** 获取所有状态正常的教师列表 */
    @Override
    public List<UserDO> listTeachers() {
        List<UserDO> teachers = list(new LambdaQueryWrapper<UserDO>()
                .eq(UserDO::getRole, CommonConstants.ROLE_TEACHER)
                .eq(UserDO::getStatus, CommonConstants.STATUS_NORMAL));
        teachers.forEach(t -> t.setPassword(null));
        return teachers;
    }
}
