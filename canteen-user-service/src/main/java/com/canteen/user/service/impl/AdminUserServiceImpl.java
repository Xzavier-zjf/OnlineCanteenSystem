package com.canteen.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.canteen.user.dto.AdminUserDTO;
import com.canteen.user.entity.User;
import com.canteen.user.mapper.UserMapper;
import com.canteen.user.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 管理员用户服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Map<String, Object> getUserList(Integer page, Integer size, String keyword, String role, String status) {
        try {
            Page<User> pageParam = new Page<>(page, size);
            QueryWrapper<User> wrapper = new QueryWrapper<>();

            // 关键词搜索
            if (StringUtils.hasText(keyword)) {
                wrapper.and(w -> w.like("username", keyword)
                        .or().like("email", keyword)
                        .or().like("phone", keyword));
            }

            // 角色筛选
            if (StringUtils.hasText(role)) {
                wrapper.eq("role", role);
            }

            // 状态筛选
            if (StringUtils.hasText(status)) {
                wrapper.eq("status", status);
            }

            wrapper.orderByDesc("create_time");

            Page<User> userPage = userMapper.selectPage(pageParam, wrapper);

            // 转换为DTO
            List<AdminUserDTO> userList = userPage.getRecords().stream()
                    .map(this::convertToAdminUserDTO)
                    .collect(Collectors.toList());

            Map<String, Object> result = new HashMap<>();
            result.put("records", userList);
            result.put("total", userPage.getTotal());
            result.put("page", page);
            result.put("size", size);
            result.put("pages", userPage.getPages());

            return result;
        } catch (Exception e) {
            log.error("获取用户列表失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("records", new ArrayList<>());
            result.put("total", 0L);
            result.put("page", page);
            result.put("size", size);
            result.put("pages", 0L);
            return result;
        }
    }

    @Override
    public AdminUserDTO getUserDetail(Long userId) {
        try {
            User user = userMapper.selectById(userId);
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }

            AdminUserDTO userDTO = convertToAdminUserDTO(user);
            
            // 如果是商户，获取额外的商户信息
            if ("MERCHANT".equals(user.getRole())) {
                // 这里可以调用其他服务获取商户详细信息
                // 暂时使用用户基本信息
            }

            return userDTO;
        } catch (Exception e) {
            log.error("获取用户详情失败：userId={}", userId, e);
            throw new RuntimeException("获取用户详情失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void updateUserStatus(Long userId, String status) {
        try {
            User user = userMapper.selectById(userId);
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }

            // 验证状态值
            if (!"ACTIVE".equals(status) && !"INACTIVE".equals(status)) {
                throw new RuntimeException("无效的状态值");
            }

            // 验证状态值并转换为对应的状态码
            Integer statusCode;
            if ("ACTIVE".equals(status)) {
                statusCode = User.Status.ENABLED.getCode();
            } else if ("INACTIVE".equals(status)) {
                statusCode = User.Status.DISABLED.getCode();
            } else {
                throw new RuntimeException("无效的状态值");
            }

            user.setStatus(statusCode);
            user.setUpdateTime(LocalDateTime.now());
            userMapper.updateById(user);

            log.info("用户状态更新成功：userId={}, status={}", userId, status);
        } catch (Exception e) {
            log.error("更新用户状态失败：userId={}, status={}", userId, status, e);
            throw new RuntimeException("更新用户状态失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public String resetUserPassword(Long userId) {
        try {
            User user = userMapper.selectById(userId);
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }

            // 生成新密码（8位随机密码）
            String newPassword = generateRandomPassword();
            String encodedPassword = passwordEncoder.encode(newPassword);

            user.setPassword(encodedPassword);
            user.setUpdateTime(LocalDateTime.now());
            userMapper.updateById(user);

            log.info("用户密码重置成功：userId={}", userId);
            return newPassword;
        } catch (Exception e) {
            log.error("重置用户密码失败：userId={}", userId, e);
            throw new RuntimeException("重置用户密码失败：" + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getUserStatistics() {
        try {
            Map<String, Object> stats = new HashMap<>();

            // 总用户数
            Long totalUsers = userMapper.selectCount(null);
            stats.put("totalUsers", totalUsers);

            // 各角色用户数
            QueryWrapper<User> userWrapper = new QueryWrapper<>();
            userWrapper.eq("role", "USER");
            Long normalUsers = userMapper.selectCount(userWrapper);
            stats.put("normalUsers", normalUsers);

            QueryWrapper<User> merchantWrapper = new QueryWrapper<>();
            merchantWrapper.eq("role", "MERCHANT");
            Long merchants = userMapper.selectCount(merchantWrapper);
            stats.put("merchants", merchants);

            QueryWrapper<User> adminWrapper = new QueryWrapper<>();
            adminWrapper.eq("role", "ADMIN");
            Long admins = userMapper.selectCount(adminWrapper);
            stats.put("admins", admins);

            // 活跃用户数
            QueryWrapper<User> activeWrapper = new QueryWrapper<>();
            activeWrapper.eq("status", User.Status.ENABLED.getCode());
            Long activeUsers = userMapper.selectCount(activeWrapper);
            stats.put("activeUsers", activeUsers);

            // 今日新增用户
            QueryWrapper<User> todayWrapper = new QueryWrapper<>();
            todayWrapper.ge("create_time", LocalDateTime.now().toLocalDate().atStartOfDay());
            Long todayNewUsers = userMapper.selectCount(todayWrapper);
            stats.put("todayNewUsers", todayNewUsers);

            return stats;
        } catch (Exception e) {
            log.error("获取用户统计信息失败", e);
            return new HashMap<>();
        }
    }

    @Override
    @Transactional
    public void approveMerchant(Long userId, Boolean approved, String reason) {
        try {
            User user = userMapper.selectById(userId);
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }

            if (!"MERCHANT".equals(user.getRole())) {
                throw new RuntimeException("该用户不是商户");
            }

            // 更新审核状态
            if (approved) {
                user.setStatus(User.Status.ENABLED.getCode());
                log.info("商户审核通过：userId={}", userId);
            } else {
                user.setStatus(User.Status.DISABLED.getCode());
                log.info("商户审核拒绝：userId={}, reason={}", userId, reason);
            }

            user.setUpdateTime(LocalDateTime.now());
            userMapper.updateById(user);

            // 这里可以发送通知给商户
            // notificationService.sendMerchantApprovalNotification(userId, approved, reason);

        } catch (Exception e) {
            log.error("审核商户失败：userId={}, approved={}", userId, approved, e);
            throw new RuntimeException("审核商户失败：" + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getPendingMerchants(Integer page, Integer size) {
        try {
            Page<User> pageParam = new Page<>(page, size);
            QueryWrapper<User> wrapper = new QueryWrapper<>();
            wrapper.eq("role", "MERCHANT");
            wrapper.eq("status", User.Status.DISABLED.getCode());
            wrapper.orderByAsc("create_time");

            Page<User> merchantPage = userMapper.selectPage(pageParam, wrapper);

            // 转换为DTO
            List<AdminUserDTO> merchantList = merchantPage.getRecords().stream()
                    .map(this::convertToAdminUserDTO)
                    .collect(Collectors.toList());

            Map<String, Object> result = new HashMap<>();
            result.put("records", merchantList);
            result.put("total", merchantPage.getTotal());
            result.put("page", page);
            result.put("size", size);
            result.put("pages", merchantPage.getPages());

            return result;
        } catch (Exception e) {
            log.error("获取待审核商户列表失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("records", new ArrayList<>());
            result.put("total", 0L);
            result.put("page", page);
            result.put("size", size);
            result.put("pages", 0L);
            return result;
        }
    }

    private AdminUserDTO convertToAdminUserDTO(User user) {
        AdminUserDTO dto = new AdminUserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setRole(user.getRole());
        dto.setRoleDesc(getRoleDescription(user.getRole()));
        dto.setStatus(user.getStatus().toString());
        dto.setStatusDesc(getStatusDescription(user.getStatus().toString()));
        dto.setCreateTime(user.getCreateTime());
        dto.setUpdateTime(user.getUpdateTime());
        // dto.setLastLoginTime(user.getLastLoginTime()); // User实体中没有此字段

        // 如果是商户，设置商户相关信息
        if ("MERCHANT".equals(user.getRole())) {
            // 这里可以从商户表获取更多信息
            dto.setMerchantName(user.getUsername() + "的店铺");
            dto.setApprovalStatus(user.getStatus().toString());
        }

        return dto;
    }

    private String getRoleDescription(String role) {
        switch (role) {
            case "USER":
                return "普通用户";
            case "MERCHANT":
                return "商户";
            case "ADMIN":
                return "管理员";
            default:
                return role;
        }
    }

    private String getStatusDescription(String status) {
        try {
            Integer statusCode = Integer.valueOf(status);
            if (statusCode.equals(User.Status.ENABLED.getCode())) {
                return "正常";
            } else if (statusCode.equals(User.Status.DISABLED.getCode())) {
                return "禁用";
            } else {
                return status;
            }
        } catch (NumberFormatException e) {
            // 如果是字符串状态，保持原有逻辑
            switch (status) {
                case "ACTIVE":
                    return "正常";
                case "INACTIVE":
                    return "禁用";
                case "PENDING":
                    return "待审核";
                case "REJECTED":
                    return "已拒绝";
                default:
                    return status;
            }
        }
    }

    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder password = new StringBuilder();
        Random random = new Random();
        
        for (int i = 0; i < 8; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return password.toString();
    }
}