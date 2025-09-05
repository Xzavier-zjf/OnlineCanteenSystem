package com.canteen.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.canteen.common.utils.JwtUtils;
import com.canteen.user.dto.AdminDTO;
import com.canteen.user.entity.User;
import com.canteen.user.mapper.UserMapper;
import com.canteen.user.service.AdminService;
import com.canteen.user.utils.TypeConversionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 管理员服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public AdminDTO.LoginResponse login(AdminDTO.LoginRequest request) {
        // 查询管理员用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, request.getUsername())
               .eq(User::getRole, User.Role.ADMIN.getCode())
               .eq(User::getStatus, User.Status.ENABLED.getCode());
        
        User admin = userMapper.selectOne(wrapper);
        if (admin == null) {
            throw new RuntimeException("管理员账号不存在或已被禁用");
        }

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        // 生成JWT token
        String token = JwtUtils.generateToken(admin.getUsername(), admin.getId(), admin.getRole());

        AdminDTO.LoginResponse response = new AdminDTO.LoginResponse();
        response.setId(admin.getId());
        response.setUsername(admin.getUsername());
        response.setToken(token);
        response.setRole(admin.getRole());

        return response;
    }

    @Override
    public AdminDTO.DashboardResponse getDashboard() {
        AdminDTO.DashboardResponse dashboard = new AdminDTO.DashboardResponse();

        try {
            // 统计用户数据
            dashboard.setTotalUsers(getUserCount("USER"));
            dashboard.setTotalMerchants(getUserCount("MERCHANT"));

            // 调用其他服务获取统计数据
            dashboard.setTotalOrders(getOrderCount());
            dashboard.setTotalProducts(getProductCount());
            dashboard.setTodaySales(getTodaySales());
            dashboard.setTotalSales(getTotalSales());
            dashboard.setOrderStats(getOrderStats());
            dashboard.setSalesStats(getSalesStats());

        } catch (Exception e) {
            log.error("获取仪表板数据失败", e);
            // 设置默认值
            dashboard.setTotalUsers(0L);
            dashboard.setTotalMerchants(0L);
            dashboard.setTotalOrders(0L);
            dashboard.setTotalProducts(0L);
            dashboard.setTodaySales(BigDecimal.ZERO);
            dashboard.setTotalSales(BigDecimal.ZERO);
            dashboard.setOrderStats(new ArrayList<>());
            dashboard.setSalesStats(new ArrayList<>());
        }

        return dashboard;
    }

    @Override
    public AdminDTO.PageResponse<AdminDTO.UserInfo> getUsers(Integer page, Integer size, String keyword, String role) {
        Page<User> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        // 搜索条件
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                           .or().like(User::getRealName, keyword)
                           .or().like(User::getEmail, keyword)
                           .or().like(User::getPhone, keyword));
        }

        if (StringUtils.hasText(role)) {
            wrapper.eq(User::getRole, role);
        }

        wrapper.orderByDesc(User::getCreateTime);

        IPage<User> userPage = userMapper.selectPage(pageParam, wrapper);

        // 转换为DTO
        List<AdminDTO.UserInfo> userInfos = userPage.getRecords().stream()
                .map(this::convertToUserInfo)
                .collect(Collectors.toList());

        AdminDTO.PageResponse<AdminDTO.UserInfo> response = new AdminDTO.PageResponse<>();
        response.setRecords(userInfos);
        response.setTotal(userPage.getTotal());
        response.setPage(page);
        response.setSize(size);
        response.setPages((int) userPage.getPages());

        return response;
    }

    @Override
    public void updateUserStatus(Long userId, Integer status) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        user.setStatus(status);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }

    @Override
    public String resetUserPassword(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 生成新密码（6位随机数字）
        String newPassword = String.format("%06d", new Random().nextInt(1000000));
        
        // 加密并更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        return newPassword;
    }

    @Override
    public AdminDTO.PageResponse<AdminDTO.MerchantInfo> getMerchants(Integer page, Integer size, String keyword) {
        Page<User> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getRole, User.Role.MERCHANT.getCode());

        // 搜索条件
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                           .or().like(User::getRealName, keyword)
                           .or().like(User::getEmail, keyword)
                           .or().like(User::getPhone, keyword));
        }

        wrapper.orderByDesc(User::getCreateTime);

        IPage<User> merchantPage = userMapper.selectPage(pageParam, wrapper);

        // 转换为DTO
        List<AdminDTO.MerchantInfo> merchantInfos = merchantPage.getRecords().stream()
                .map(this::convertToMerchantInfo)
                .collect(Collectors.toList());

        AdminDTO.PageResponse<AdminDTO.MerchantInfo> response = new AdminDTO.PageResponse<>();
        response.setRecords(merchantInfos);
        response.setTotal(merchantPage.getTotal());
        response.setPage(page);
        response.setSize(size);
        response.setPages((int) merchantPage.getPages());

        return response;
    }

    @Override
    public void approveMerchant(Long merchantId, Boolean approved, String reason) {
        User merchant = userMapper.selectById(merchantId);
        if (merchant == null || !User.Role.MERCHANT.getCode().equals(merchant.getRole())) {
            throw new RuntimeException("商户不存在");
        }

        // 更新商户状态
        merchant.setStatus(approved ? User.Status.ENABLED.getCode() : User.Status.DISABLED.getCode());
        merchant.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(merchant);

        log.info("商户审核完成：merchantId={}, approved={}, reason={}", merchantId, approved, reason);
    }

    // 私有辅助方法
    private Long getUserCount(String role) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getRole, role);
        return userMapper.selectCount(wrapper);
    }

    private Long getOrderCount() {
        try {
            // 调用订单服务获取总订单数
            return restTemplate.getForObject("http://localhost:8082/api/orders/count", Long.class);
        } catch (Exception e) {
            log.warn("获取订单总数失败", e);
            return 0L;
        }
    }

    private Long getProductCount() {
        try {
            // 调用商品服务获取总商品数
            return restTemplate.getForObject("http://localhost:8081/api/products/count", Long.class);
        } catch (Exception e) {
            log.warn("获取商品总数失败", e);
            return 0L;
        }
    }

    private BigDecimal getTodaySales() {
        try {
            // 调用订单服务获取今日销售额
            return restTemplate.getForObject("http://localhost:8082/api/orders/sales/today", BigDecimal.class);
        } catch (Exception e) {
            log.warn("获取今日销售额失败", e);
            return BigDecimal.ZERO;
        }
    }

    private BigDecimal getTotalSales() {
        try {
            // 调用订单服务获取总销售额
            return restTemplate.getForObject("http://localhost:8082/api/orders/sales/total", BigDecimal.class);
        } catch (Exception e) {
            log.warn("获取总销售额失败", e);
            return BigDecimal.ZERO;
        }
    }

    private List<AdminDTO.OrderStats> getOrderStats() {
        try {
            // 调用订单服务获取订单统计
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> stats = restTemplate.getForObject("http://localhost:8082/api/orders/stats/status", List.class);

            return stats.stream().map(stat -> {
                AdminDTO.OrderStats orderStats = new AdminDTO.OrderStats();
                orderStats.setStatus((String) stat.get("status"));
                orderStats.setCount(TypeConversionUtil.toLong(stat.get("count")));
                return orderStats;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("获取订单统计失败", e);
            return new ArrayList<>();
        }
    }

    private List<AdminDTO.SalesStats> getSalesStats() {
        try {
            // 调用订单服务获取销售统计（最近7天）
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> stats = restTemplate.getForObject("http://localhost:8082/api/orders/stats/sales?days=7", List.class);

            return stats.stream().map(stat -> {
                AdminDTO.SalesStats salesStats = new AdminDTO.SalesStats();
                salesStats.setDate((String) stat.get("date"));
                salesStats.setAmount(new BigDecimal(stat.get("amount").toString()));
                salesStats.setOrderCount(TypeConversionUtil.toLong(stat.get("orderCount")));
                return salesStats;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("获取销售统计失败", e);
            return new ArrayList<>();
        }
    }

    private AdminDTO.UserInfo convertToUserInfo(User user) {
        AdminDTO.UserInfo userInfo = new AdminDTO.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setEmail(user.getEmail());
        userInfo.setPhone(user.getPhone());
        userInfo.setRealName(user.getRealName());
        userInfo.setCollege(user.getCollege());
        userInfo.setRole(user.getRole());
        userInfo.setStatus(user.getStatus());
        userInfo.setCreateTime(user.getCreateTime());

        // 获取用户订单统计（可选）
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> stats = restTemplate.getForObject("http://localhost:8082/api/orders/user/" + user.getId() + "/stats", Map.class);
            
            if (stats != null) {
                userInfo.setTotalOrders(TypeConversionUtil.toLong(stats.get("totalOrders")));
                userInfo.setTotalSpent(new BigDecimal(stats.get("totalSpent").toString()));
            }
        } catch (Exception e) {
            log.debug("获取用户统计失败：userId={}", user.getId());
            userInfo.setTotalOrders(0L);
            userInfo.setTotalSpent(BigDecimal.ZERO);
        }

        return userInfo;
    }

    private AdminDTO.MerchantInfo convertToMerchantInfo(User user) {
        AdminDTO.MerchantInfo merchantInfo = new AdminDTO.MerchantInfo();
        merchantInfo.setId(user.getId());
        merchantInfo.setUsername(user.getUsername());
        merchantInfo.setEmail(user.getEmail());
        merchantInfo.setPhone(user.getPhone());
        merchantInfo.setRealName(user.getRealName());
        merchantInfo.setShopName(user.getRealName()); // 暂时使用真实姓名作为店铺名
        merchantInfo.setStatus(user.getStatus());
        merchantInfo.setCreateTime(user.getCreateTime());

        // 获取商户统计（可选）
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> stats = restTemplate.getForObject("http://localhost:8081/api/products/merchant/" + user.getId() + "/stats", Map.class);
            
            if (stats != null) {
                merchantInfo.setTotalProducts(TypeConversionUtil.toLong(stats.get("totalProducts")));
                merchantInfo.setTotalOrders(TypeConversionUtil.toLong(stats.get("totalOrders")));
                merchantInfo.setTotalSales(new BigDecimal(stats.get("totalSales").toString()));
            }
        } catch (Exception e) {
            log.debug("获取商户统计失败：merchantId={}", user.getId());
            merchantInfo.setTotalProducts(0L);
            merchantInfo.setTotalOrders(0L);
            merchantInfo.setTotalSales(BigDecimal.ZERO);
        }

        return merchantInfo;
    }
}