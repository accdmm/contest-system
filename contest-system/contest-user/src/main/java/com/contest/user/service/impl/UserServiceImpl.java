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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import cn.hutool.crypto.digest.DigestUtil;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 用户服务实现
 *
 * 处理用户注册、登录、密码管理、账号冻结等业务逻辑。
 * 注册时做唯一性校验和 BCrypt 加密，登录时检查账号状态。
 *
 * 安全性说明：
 * - 密码使用 BCrypt 算法（Hutool DigestUtil）加密存储，不可逆哈希
 * - 登录时通过 BCrypt.checkPassword 比对，不解密原始密码
 * - 密码强度校验：8-20 位，必须包含字母和数字
 * - 学号（username）、邮箱（email）、手机号（phone）均建立唯一索引，防止重复注册
 * - 账号冻结状态（status=1）在登录时前置检查
 * - 所有数据库操作使用 MyBatis-Plus 参数化查询，防 SQL 注入
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final CollegeMapper collegeMapper;
    private final MajorMapper majorMapper;

    public UserServiceImpl(CollegeMapper collegeMapper, MajorMapper majorMapper) {
        this.collegeMapper = collegeMapper;
        this.majorMapper = majorMapper;
    }

    /**
     * 用户登录：校验用户名密码、检查账号状态
     *
     * 查询流程：学号存在性 → 账号冻结检查 → 密码 BCrypt 比对。
     * 密码错误和账号不存在均返回同一提示，防止账号枚举攻击。
     * 冻结账号在登录时直接拦截，不暴露更详细的错误原因。
     */
    @Override
    @Transactional(readOnly = true)
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

    /**
     * 校验密码强度：8-20位，需包含字母和数字
     *
     * 前后端校验规则保持一致。使用正则表达式逐一检查字母和数字的存在性，
     * 而非一次性 ^(?=.*[a-zA-Z])(?=.*\d).{8,20}$ 正则，便于错误提示定位。
     */
    private void validatePassword(String password) {
        if (password == null || password.length() < 8 || password.length() > 20) {
            throw new BusinessException("密码长度需为8-20位");
        }
        if (!Pattern.compile("[a-zA-Z]").matcher(password).find()
                || !Pattern.compile("[0-9]").matcher(password).find()) {
            throw new BusinessException("密码需同时包含字母和数字");
        }
    }

    /**
     * 用户注册：校验唯一性、BCrypt加密、默认角色为学生
     *
     * 校验顺序：密码强度 → 学号唯一 → 邮箱唯一（可选）→ 手机号唯一（可选），
     * 尽早阻断无效请求。注册时自动设置默认角色为 0（学生）、状态为 0（正常）。
     * 同步写入学院/专业的文字冗余字段，避免后续关联查询。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
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

    /**
     * 管理员创建用户：校验唯一性和角色值，BCrypt加密
     *
     * Controller 层已校验非管理员不能创建管理员账号（ROLE_ADMIN 角色检查）。
     * 此方法校验角色值必须在 [0, 2] 范围内，防止越权传入非法角色。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
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

    /**
     * 修改用户资料：同步学院/专业冗余文字字段
     *
     * 处理要点：
     * - 防止前端提交空字符串覆盖已有头像（avatarUrl 为空时设为 null，不更新数据库）
     * - 通过 collegeId 反查 college 名称，同步更新冗余字段（避免每次查询都 JOIN）
     * - 设置 id、password、username、status 为 null，防止覆盖数据库已有值
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
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

    /**
     * 修改密码：校验原密码后更新为新密码
     *
     * 先通过 BCrypt.checkPassword 校验原密码，新密码需通过 validatePassword
     * 强度校验后再进行 BCrypt 加密存储。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
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

    /**
     * 冻结用户：将账号状态置为 1（冻结），登录时被拦截
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void freezeUser(Long userId) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setStatus(CommonConstants.STATUS_FROZEN);
        updateById(user);
    }

    /**
     * 解冻用户：恢复账号状态为 0（正常）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unfreezeUser(Long userId) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setStatus(CommonConstants.STATUS_NORMAL);
        updateById(user);
    }

    /**
     * 分页查询用户：支持按用户名、姓名、学院、专业模糊搜索
     *
     * 使用 MyBatis-Plus 分页插件自动添加 LIMIT 和 COUNT 查询。
     * 返回前清除密码字段（setPassword(null)），防止密码泄露。
     * 性能说明：user 表有 idx_college、idx_status 索引，like 查询在数据量较小时性能可接受。
     */
    @Override
    @Transactional(readOnly = true)
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

    /**
     * 获取所有状态正常的教师列表
     *
     * 仅返回 status=0（正常）的教师，冻结教师不可被选为指导教师。
     */
    @Override
    @Transactional(readOnly = true)
    public List<User> listTeachers() {
        List<User> teachers = list(new LambdaQueryWrapper<User>()
                .eq(User::getRole, CommonConstants.ROLE_TEACHER)
                .eq(User::getStatus, CommonConstants.STATUS_NORMAL));
        teachers.forEach(t -> t.setPassword(null));
        return teachers;
    }
}
