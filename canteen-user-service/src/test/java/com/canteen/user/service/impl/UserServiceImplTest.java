package com.canteen.user.service.impl;

import com.canteen.user.dto.UserDTO;
import com.canteen.user.entity.User;
import com.canteen.user.mapper.UserMapper;
import com.canteen.user.service.UserService;
import com.canteen.common.utils.JwtUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户服务测试类
 */
public class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;
    
    private UserService userService;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userService = new UserServiceImpl(userMapper);
    }
    
    @Test
    void testRegisterSuccess() {
        // 准备测试数据
        UserDTO.RegisterRequest request = new UserDTO.RegisterRequest();
        request.setUsername("testuser");
        request.setPassword("password123");
        request.setEmail("test@example.com");
        request.setPhone("13800000001");
        request.setRealName("测试用户");
        
        // Mock 行为
        when(userMapper.selectOne(any())).thenReturn(null); // 用户不存在
        when(userMapper.insert(any(User.class))).thenReturn(1); // 插入成功
        
        // 执行测试
        boolean result = userService.register(request);
        
        // 验证结果
        assertTrue(result);
        verify(userMapper, times(2)).selectOne(any()); // 检查用户名和邮箱
        verify(userMapper, times(1)).insert(any(User.class));
    }
    
    @Test
    void testLoginSuccess() {
        // 准备测试数据
        UserDTO.LoginRequest request = new UserDTO.LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");
        
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("password123");
        user.setRealName("测试用户");
        user.setRole("USER");
        user.setStatus(1);
        
        // Mock 行为
        when(userMapper.selectOne(any())).thenReturn(user);
        
        // 执行测试
        UserDTO.LoginResponse response = userService.login(request);
        
        // 验证结果
        assertNotNull(response);
        assertEquals("testuser", response.getUsername());
        assertNotNull(response.getToken()); // 只验证token不为空
        assertEquals("USER", response.getRole());
    }
    
    @Test
    void testPasswordVerification() {
        UserServiceImpl serviceImpl = (UserServiceImpl) userService;
        
        // 测试明文密码验证
        assertTrue(serviceImpl.verifyPassword("password123", "password123"));
        assertFalse(serviceImpl.verifyPassword("password123", "wrongpassword"));
    }
}