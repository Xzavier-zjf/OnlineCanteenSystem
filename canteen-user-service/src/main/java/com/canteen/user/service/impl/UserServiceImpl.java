package com.canteen.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.canteen.common.exception.BusinessException;
import com.canteen.common.utils.JwtUtils;
import com.canteen.user.dto.UserDTO;
import com.canteen.user.entity.User;
import com.canteen.user.mapper.UserMapper;
import com.canteen.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    // 暂时使用明文密码用于测试
    // private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    @Value("${canteen.order-service.url:http://localhost:8082}")
    private String orderServiceUrl;
    
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    @Transactional
    public boolean register(UserDTO.RegisterRequest request) {
        try {
            log.info("开始注册用户: {}", request.getUsername());
            
            // 检查用户名是否已存在
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("username", request.getUsername());
            User existUser = userMapper.selectOne(queryWrapper);
            if (existUser != null) {
                throw new BusinessException("用户名已存在");
            }

            // 检查邮箱是否已存在
            if (request.getEmail() != null && !request.getEmail().isEmpty()) {
                QueryWrapper<User> emailWrapper = new QueryWrapper<>();
                emailWrapper.eq("email", request.getEmail());
                User existEmailUser = userMapper.selectOne(emailWrapper);
                if (existEmailUser != null) {
                    throw new BusinessException("邮箱已被注册");
                }
            }

            // 创建用户
            User user = new User();
            BeanUtils.copyProperties(request, user);
            // 暂时使用明文密码
            user.setPassword(request.getPassword()); // passwordEncoder.encode(request.getPassword())
            user.setRole(User.Role.USER.getCode());
            user.setStatus(User.Status.ENABLED.getCode());
            // 不手动设置时间，让MyBatis-Plus自动填充
            // user.setCreateTime(LocalDateTime.now());
            // user.setUpdateTime(LocalDateTime.now());

            log.info("准备插入用户数据: {}", user);
            int result = userMapper.insert(user);
            log.info("用户注册成功: {}, 插入结果: {}", request.getUsername(), result);
            return result > 0;
        } catch (Exception e) {
            log.error("用户注册失败: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public UserDTO.LoginResponse login(UserDTO.LoginRequest request) {
        // 查找用户
        User user = findByUsername(request.getUsername());
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }

        // 验证密码
        if (!verifyPassword(request.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 检查用户状态
        if (user.getStatus() != User.Status.ENABLED.getCode()) {
            throw new BusinessException("用户账号已被禁用");
        }

        // 生成JWT Token
        String token = JwtUtils.generateToken(user.getUsername(), user.getId(), user.getRole());

        // 构建登录响应
        UserDTO.LoginResponse response = new UserDTO.LoginResponse();
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setRealName(user.getRealName());
        response.setRole(user.getRole());
        response.setToken(token);

        log.info("用户登录成功: {}", request.getUsername());
        return response;
    }

    @Override
    public UserDTO.UserInfoResponse getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        UserDTO.UserInfoResponse response = new UserDTO.UserInfoResponse();
        BeanUtils.copyProperties(user, response);
        return response;
    }

    @Override
    @Transactional
    public boolean updateUserInfo(Long userId, UserDTO.UpdateUserRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 检查邮箱是否被其他用户使用
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            QueryWrapper<User> emailWrapper = new QueryWrapper<>();
            emailWrapper.eq("email", request.getEmail());
            emailWrapper.ne("id", userId);
            User existEmailUser = userMapper.selectOne(emailWrapper);
            if (existEmailUser != null) {
                throw new BusinessException("邮箱已被其他用户使用");
            }
        }

        // 更新用户信息
        BeanUtils.copyProperties(request, user);
        user.setUpdateTime(LocalDateTime.now());

        int result = userMapper.updateById(user);
        log.info("用户信息更新成功: {}", userId);
        return result > 0;
    }

    @Override
    public User findByUsername(String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        return userMapper.selectOne(queryWrapper);
    }

    @Override
    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        // 暂时使用明文密码比较
        return rawPassword.equals(encodedPassword);
        // return passwordEncoder.matches(rawPassword, encodedPassword);
    }
    
    @Override
    @Transactional
    public boolean changePassword(Long userId, UserDTO.ChangePasswordRequest request) {
        // 获取用户信息
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 验证原密码
        if (!verifyPassword(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException("原密码不正确");
        }
        
        // 更新密码
        user.setPassword(request.getNewPassword()); // 暂时使用明文密码
        // user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdateTime(LocalDateTime.now());
        
        int result = userMapper.updateById(user);
        log.info("用户密码修改成功: {}", userId);
        return result > 0;
    }
    
    @Override
    public UserDTO.UserStatsResponse getUserStats(Long userId) {
        try {
            // 调用订单服务获取统计数据
            String url = orderServiceUrl + "/api/orders/stats/user/" + userId;
            log.info("调用订单服务获取统计数据: {}", url);
            
            // 调用订单服务API
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            UserDTO.UserStatsResponse stats = new UserDTO.UserStatsResponse();
            
            if (response != null && "200".equals(String.valueOf(response.get("code")))) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) response.get("data");
                if (data != null) {
                    stats.setTotalOrders((Integer) data.get("totalOrders"));
                    stats.setTotalAmount((String) data.get("totalAmount"));
                    stats.setFavoriteCount((Integer) data.get("favoriteCount"));
                }
            } else {
                // 如果订单服务不可用，返回默认值
                log.warn("订单服务不可用，返回默认统计数据");
                stats.setTotalOrders(0);
                stats.setTotalAmount("0.00");
                stats.setFavoriteCount(0);
            }
            
            return stats;
        } catch (Exception e) {
            log.error("获取用户统计数据失败: {}", e.getMessage(), e);
            // 返回默认值
            UserDTO.UserStatsResponse stats = new UserDTO.UserStatsResponse();
            stats.setTotalOrders(0);
            stats.setTotalAmount("0.00");
            stats.setFavoriteCount(0);
            return stats;
        }
    }
}
