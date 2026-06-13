package com.contest.user.service.impl;

import com.contest.common.constant.CommonConstants;
import com.contest.common.exception.BusinessException;
import com.contest.user.entity.User;
import com.contest.user.service.UserService;
import com.contest.user.test.TestApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = TestApplication.class)
@ActiveProfiles("test")
@Transactional
class UserServiceImplTest {

    @Autowired
    private UserService userService;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        jdbc.execute("INSERT INTO college (id, name) VALUES (1, '计算机学院')");
        jdbc.execute("INSERT INTO major (id, college_id, name) VALUES (1, 1, '软件工程')");
    }

    @Test
    void register_shouldThrowWhenUsernameExists() {
        jdbc.execute("INSERT INTO `user` (id, username, password, name, role, status) VALUES (1, 'existing', 'pw', '已有用户', 0, 0)");
        User user = new User();
        user.setUsername("existing");
        user.setName("新用户");
        assertThrows(BusinessException.class, () -> userService.register(user, "password123"));
    }

    @Test
    void register_shouldThrowWhenEmailExists() {
        jdbc.execute("INSERT INTO `user` (id, username, password, name, email, role, status) VALUES (1, 'user1', 'pw', '用户A', 'test@test.com', 0, 0)");
        User user = new User();
        user.setUsername("newuser");
        user.setName("新用户");
        user.setEmail("test@test.com");
        assertThrows(BusinessException.class, () -> userService.register(user, "password123"));
    }

    @Test
    void register_shouldThrowWhenPasswordTooShort() {
        User user = new User();
        user.setUsername("newuser");
        user.setName("新用户");
        assertThrows(BusinessException.class, () -> userService.register(user, "1234567"));
    }

    @Test
    void register_shouldSucceed() {
        User user = new User();
        user.setUsername("newuser");
        user.setName("新用户");
        user.setCollege("计算机学院");
        user.setMajor("软件工程");
        User result = userService.register(user, "password123");
        assertNotNull(result.getId());
        assertEquals("newuser", result.getUsername());
        assertEquals(CommonConstants.ROLE_STUDENT, result.getRole());
    }

    @Test
    void login_shouldThrowWhenUserNotFound() {
        assertThrows(BusinessException.class, () -> userService.login("nonexistent", "password123"));
    }

    @Test
    void login_shouldThrowWhenWrongPassword() {
        jdbc.execute("INSERT INTO `user` (id, username, password, name, role, status) VALUES (1, 'testuser', '$2a$10$dummy', '测试用户', 0, 0)");
        assertThrows(BusinessException.class, () -> userService.login("testuser", "wrongpassword"));
    }

    @Test
    void updateProfile_shouldSucceed() {
        jdbc.execute("INSERT INTO `user` (id, username, password, name, role, status) VALUES (1, 'user1', 'pw', '用户A', 0, 0)");
        User update = new User();
        update.setName("新名字");
        update.setEmail("new@test.com");
        userService.updateProfile(1L, update);
        User updated = userService.getById(1L);
        assertEquals("新名字", updated.getName());
        assertEquals("new@test.com", updated.getEmail());
    }
}
