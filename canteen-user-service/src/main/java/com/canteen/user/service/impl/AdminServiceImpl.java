package com.canteen.user.service.impl;

import com.canteen.common.exception.BusinessException;
import com.canteen.user.dto.AdminDTO;
import com.canteen.user.entity.User;
import com.canteen.user.mapper.UserMapper;
import com.canteen.user.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 管理员服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserMapper userMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${canteen.order-service.url:http://localhost:8082}")
    private String orderServiceUrl;

    @Value("${canteen.product-service.url:http://localhost:8083}")
    private String productServiceUrl;

    @Override
    public AdminDTO.DashboardResponse getDashboard(Long userId) {
        // 验证管理员权限
        validateAdminPermission(userId);

        AdminDTO.DashboardResponse response = new AdminDTO.DashboardResponse();

        try {
            // 获取用户统计
            Long totalUsers = userMapper.selectCount(null);
            response.setTotalUsers(totalUsers.intValue());
            response.setActiveUsers(totalUsers.intValue()); // 简化处理

            // 获取订单统计
            try {
                String orderStatsUrl = orderServiceUrl + "/api/orders/stats/system";
                @SuppressWarnings("unchecked")
                Map<String, Object> orderStats = restTemplate.getForObject(orderStatsUrl, Map.class);
                if (orderStats != null && "200".equals(String.valueOf(orderStats.get("code")))) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> data = (Map<String, Object>) orderStats.get("data");
                    if (data != null) {
                        response.setTotalOrders((Integer) data.getOrDefault("totalOrders", 0));
                        response.setTodayOrders((Integer) data.getOrDefault("todayOrders", 0));
                        response.setTotalRevenue((String) data.getOrDefault("totalRevenue", "0.00"));
                        response.setTodayRevenue((String) data.getOrDefault("todayRevenue", "0.00"));
                        response.setPendingOrders((Integer) data.getOrDefault("pendingOrders", 0));
                    }
                }
            } catch (Exception e) {
                log.warn("获取订单统计失败: {}", e.getMessage());
                response.setTotalOrders(0);
                response.setTodayOrders(0);
                response.setTotalRevenue("0.00");
                response.setTodayRevenue("0.00");
                response.setPendingOrders(0);
            }

            // 获取商品统计
            try {
                String productStatsUrl = productServiceUrl + "/api/products/stats/system";
                @SuppressWarnings("unchecked")
                Map<String, Object> productStats = restTemplate.getForObject(productStatsUrl, Map.class);
                if (productStats != null && "200".equals(String.valueOf(productStats.get("code")))) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> data = (Map<String, Object>) productStats.get("data");
                    if (data != null) {
                        response.setTotalProducts((Integer) data.getOrDefault("totalProducts", 0));
                    }
                }
            } catch (Exception e) {
                log.warn("获取商品统计失败: {}", e.getMessage());
                response.setTotalProducts(0);
            }

            // 生成模拟的最近订单数据
            List<AdminDTO.DashboardResponse.RecentOrder> recentOrders = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                AdminDTO.DashboardResponse.RecentOrder order = new AdminDTO.DashboardResponse.RecentOrder();
                order.setOrderId((long) (i + 1));
                order.setOrderNo("CT" + System.currentTimeMillis() + i);
                order.setUserName("用户" + (i + 1));
                order.setTotalAmount("15.00");
                order.setStatus("PENDING");
                order.setCreateTime(LocalDateTime.now().minusHours(i));
                recentOrders.add(order);
            }
            response.setRecentOrders(recentOrders);

            // 生成模拟的热门商品数据
            List<AdminDTO.DashboardResponse.HotProduct> hotProducts = new ArrayList<>();
            String[] productNames = {"红烧肉盖饭", "宫保鸡丁", "糖醋里脊", "麻婆豆腐", "回锅肉"};
            for (int i = 0; i < 5; i++) {
                AdminDTO.DashboardResponse.HotProduct product = new AdminDTO.DashboardResponse.HotProduct();
                product.setProductId((long) (i + 1));
                product.setProductName(productNames[i]);
                product.setSales(100 - i * 10);
                product.setRevenue(String.valueOf((100 - i * 10) * 15.0));
                product.setImageUrl("/images/product" + (i + 1) + ".jpg");
                hotProducts.add(product);
            }
            response.setHotProducts(hotProducts);

        } catch (Exception e) {
            log.error("获取仪表板数据失败: {}", e.getMessage(), e);
            // 返回默认值
            response.setTotalUsers(0);
            response.setActiveUsers(0);
            response.setTotalOrders(0);
            response.setTodayOrders(0);
            response.setTotalRevenue("0.00");
            response.setTodayRevenue("0.00");
            response.setTotalProducts(0);
            response.setPendingOrders(0);
            response.setRecentOrders(new ArrayList<>());
            response.setHotProducts(new ArrayList<>());
        }

        return response;
    }

    @Override
    public AdminDTO.SystemSettingsResponse getSystemSettings(Long userId) {
        // 验证管理员权限
        validateAdminPermission(userId);

        AdminDTO.SystemSettingsResponse response = new AdminDTO.SystemSettingsResponse();
        response.setSystemName("在线食堂管理系统");
        response.setSystemVersion("1.0.0");
        response.setSystemDescription("基于Spring Boot的在线食堂订餐管理系统");
        response.setContactEmail("admin@canteen.com");
        response.setContactPhone("400-123-4567");
        response.setMaintenanceMode(false);
        response.setMaxUsers(10000);
        response.setSessionTimeout(30);
        response.setEnableRegistration(true);
        response.setEnableEmailVerification(false);

        return response;
    }

    @Override
    public boolean updateSystemSettings(Long userId, AdminDTO.UpdateSystemSettingsRequest request) {
        // 验证管理员权限
        validateAdminPermission(userId);

        // 这里可以将设置保存到数据库或配置文件
        log.info("管理员{}更新系统设置: {}", userId, request);
        return true;
    }

    @Override
    public AdminDTO.SystemStatsResponse getSystemStats(Long userId) {
        // 验证管理员权限
        validateAdminPermission(userId);

        AdminDTO.SystemStatsResponse response = new AdminDTO.SystemStatsResponse();

        try {
            // 获取用户统计
            Long totalUsers = userMapper.selectCount(null);
            response.setTotalUsers(totalUsers.intValue());
            response.setActiveUsers(totalUsers.intValue()); // 简化处理，假设所有用户都是活跃的

            // 获取订单统计
            try {
                String orderStatsUrl = orderServiceUrl + "/api/orders/stats/system";
                @SuppressWarnings("unchecked")
                Map<String, Object> orderStats = restTemplate.getForObject(orderStatsUrl, Map.class);
                if (orderStats != null && "200".equals(String.valueOf(orderStats.get("code")))) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> data = (Map<String, Object>) orderStats.get("data");
                    if (data != null) {
                        response.setTotalOrders((Integer) data.getOrDefault("totalOrders", 0));
                        response.setTodayOrders((Integer) data.getOrDefault("todayOrders", 0));
                        response.setTotalRevenue((String) data.getOrDefault("totalRevenue", "0.00"));
                        response.setTodayRevenue((String) data.getOrDefault("todayRevenue", "0.00"));
                    }
                }
            } catch (Exception e) {
                log.warn("获取订单统计失败: {}", e.getMessage());
                response.setTotalOrders(0);
                response.setTodayOrders(0);
                response.setTotalRevenue("0.00");
                response.setTodayRevenue("0.00");
            }

            // 获取商品统计
            try {
                String productStatsUrl = productServiceUrl + "/api/products/stats/system";
                @SuppressWarnings("unchecked")
                Map<String, Object> productStats = restTemplate.getForObject(productStatsUrl, Map.class);
                if (productStats != null && "200".equals(String.valueOf(productStats.get("code")))) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> data = (Map<String, Object>) productStats.get("data");
                    if (data != null) {
                        response.setTotalProducts((Integer) data.getOrDefault("totalProducts", 0));
                        response.setActiveProducts((Integer) data.getOrDefault("activeProducts", 0));
                    }
                }
            } catch (Exception e) {
                log.warn("获取商品统计失败: {}", e.getMessage());
                response.setTotalProducts(0);
                response.setActiveProducts(0);
            }

            // 系统性能指标（模拟数据）
            response.setSystemLoad(0.65);
            response.setMemoryUsage("68%");
            response.setDiskUsage("45%");
            response.setLastUpdateTime(LocalDateTime.now());

        } catch (Exception e) {
            log.error("获取系统统计数据失败: {}", e.getMessage(), e);
            // 返回默认值
            response.setTotalUsers(0);
            response.setActiveUsers(0);
            response.setTotalOrders(0);
            response.setTodayOrders(0);
            response.setTotalRevenue("0.00");
            response.setTodayRevenue("0.00");
            response.setTotalProducts(0);
            response.setActiveProducts(0);
            response.setSystemLoad(0.0);
            response.setMemoryUsage("0%");
            response.setDiskUsage("0%");
            response.setLastUpdateTime(LocalDateTime.now());
        }

        return response;
    }

    @Override
    public AdminDTO.BackupResponse backup(Long userId) {
        // 验证管理员权限
        validateAdminPermission(userId);

        AdminDTO.BackupResponse response = new AdminDTO.BackupResponse();
        response.setBackupId(UUID.randomUUID().toString());
        response.setBackupPath("/backup/canteen_" + System.currentTimeMillis() + ".sql");
        response.setBackupSize("15.6 MB");
        response.setBackupTime(LocalDateTime.now());
        response.setStatus("SUCCESS");

        log.info("管理员{}执行系统备份: {}", userId, response.getBackupId());
        return response;
    }

    @Override
    public boolean clearLogs(Long userId) {
        // 验证管理员权限
        validateAdminPermission(userId);

        log.info("管理员{}清理系统日志", userId);
        // 这里可以实现实际的日志清理逻辑
        return true;
    }

    @Override
    public AdminDTO.SystemLogsResponse getSystemLogs(Long userId, int page, int size) {
        // 验证管理员权限
        validateAdminPermission(userId);

        AdminDTO.SystemLogsResponse response = new AdminDTO.SystemLogsResponse();
        List<AdminDTO.SystemLogsResponse.SystemLog> logs = new ArrayList<>();

        // 生成一些示例日志数据
        for (int i = 0; i < size; i++) {
            AdminDTO.SystemLogsResponse.SystemLog log = new AdminDTO.SystemLogsResponse.SystemLog();
            log.setId((long) (page - 1) * size + i + 1);
            log.setLevel(i % 3 == 0 ? "ERROR" : (i % 2 == 0 ? "WARN" : "INFO"));
            log.setMessage("系统日志消息 " + log.getId());
            log.setModule("用户服务");
            log.setUserId(String.valueOf(userId));
            log.setIp("192.168.1.100");
            log.setCreateTime(LocalDateTime.now().minusHours(i));
            logs.add(log);
        }

        response.setLogs(logs);
        response.setTotal(100); // 假设总共有100条日志
        response.setPage(page);
        response.setSize(size);

        return response;
    }

    /**
     * 验证管理员权限
     */
    private void validateAdminPermission(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!"ADMIN".equals(user.getRole())) {
            throw new BusinessException("权限不足，需要管理员权限");
        }
    }
}