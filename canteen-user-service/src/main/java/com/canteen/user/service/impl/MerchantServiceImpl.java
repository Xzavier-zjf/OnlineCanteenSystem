package com.canteen.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.canteen.common.utils.JwtUtils;
import com.canteen.user.dto.MerchantDTO;
import com.canteen.user.entity.User;
import com.canteen.user.mapper.UserMapper;
import com.canteen.user.service.MerchantService;
import com.canteen.user.utils.TypeConversionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商户服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantServiceImpl implements MerchantService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public MerchantDTO.LoginResponse login(MerchantDTO.LoginRequest request) {
        // 查询商户用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, request.getUsername())
               .eq(User::getRole, User.Role.MERCHANT.getCode());
        
        User merchant = userMapper.selectOne(wrapper);
        if (merchant == null) {
            throw new RuntimeException("商户账号不存在");
        }

        if (merchant.getStatus() == User.Status.DISABLED.getCode()) {
            throw new RuntimeException("商户账号已被禁用或待审核");
        }

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), merchant.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        // 生成JWT token
        String token = JwtUtils.generateToken(merchant.getUsername(), merchant.getId(), merchant.getRole());

        MerchantDTO.LoginResponse response = new MerchantDTO.LoginResponse();
        response.setId(merchant.getId());
        response.setUsername(merchant.getUsername());
        response.setToken(token);
        response.setRole(merchant.getRole());
        response.setStatus(merchant.getStatus());
        response.setShopName(merchant.getRealName()); // 暂时使用真实姓名作为店铺名

        return response;
    }

    @Override
    public void register(MerchantDTO.RegisterRequest request) {
        // 检查用户名是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, request.getUsername());
        if (userMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (StringUtils.hasText(request.getEmail())) {
            wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getEmail, request.getEmail());
            if (userMapper.selectCount(wrapper) > 0) {
                throw new RuntimeException("邮箱已被使用");
            }
        }

        // 创建商户用户
        User merchant = new User();
        merchant.setUsername(request.getUsername());
        merchant.setPassword(passwordEncoder.encode(request.getPassword()));
        merchant.setEmail(request.getEmail());
        merchant.setPhone(request.getPhone());
        merchant.setRealName(request.getRealName());
        merchant.setAddress(request.getAddress());
        merchant.setRole(User.Role.MERCHANT.getCode());
        merchant.setStatus(User.Status.DISABLED.getCode()); // 待审核状态
        merchant.setCreateTime(LocalDateTime.now());
        merchant.setUpdateTime(LocalDateTime.now());

        userMapper.insert(merchant);
        log.info("商户注册成功：username={}, realName={}", request.getUsername(), request.getRealName());
    }

    @Override
    public MerchantDTO.DashboardResponse getDashboard(Long merchantId) {
        MerchantDTO.DashboardResponse dashboard = new MerchantDTO.DashboardResponse();

        try {
            // 获取待处理订单数
            dashboard.setPendingOrders(getPendingOrderCount(merchantId));
            
            // 获取今日营业额
            dashboard.setTodayRevenue(getTodayRevenue(merchantId));
            
            // 获取商品总数
            dashboard.setTotalProducts(getTotalProductCount(merchantId));
            
            // 获取总订单数
            dashboard.setTotalOrders(getTotalOrderCount(merchantId));
            
            // 获取热销商品
            dashboard.setTopProducts(getTopProducts(merchantId));
            
            // 获取订单趋势
            dashboard.setOrderTrends(getOrderTrends(merchantId));

        } catch (Exception e) {
            log.error("获取商户仪表板数据失败：merchantId={}", merchantId, e);
            // 设置默认值
            dashboard.setPendingOrders(0L);
            dashboard.setTodayRevenue(BigDecimal.ZERO);
            dashboard.setTotalProducts(0L);
            dashboard.setTotalOrders(0L);
            dashboard.setTopProducts(new ArrayList<>());
            dashboard.setOrderTrends(new ArrayList<>());
        }

        return dashboard;
    }

    @Override
    public MerchantDTO.MerchantInfo getMerchantInfo(Long merchantId) {
        User merchant = userMapper.selectById(merchantId);
        if (merchant == null || !User.Role.MERCHANT.getCode().equals(merchant.getRole())) {
            throw new RuntimeException("商户不存在");
        }

        MerchantDTO.MerchantInfo merchantInfo = new MerchantDTO.MerchantInfo();
        merchantInfo.setId(merchant.getId());
        merchantInfo.setUsername(merchant.getUsername());
        merchantInfo.setEmail(merchant.getEmail());
        merchantInfo.setPhone(merchant.getPhone());
        merchantInfo.setRealName(merchant.getRealName());
        merchantInfo.setShopName(merchant.getRealName()); // 暂时使用真实姓名作为店铺名
        merchantInfo.setAddress(merchant.getAddress());
        merchantInfo.setStatus(merchant.getStatus());
        merchantInfo.setCreateTime(merchant.getCreateTime());

        return merchantInfo;
    }

    @Override
    public void updateMerchantInfo(Long merchantId, MerchantDTO.UpdateInfoRequest request) {
        User merchant = userMapper.selectById(merchantId);
        if (merchant == null || !User.Role.MERCHANT.getCode().equals(merchant.getRole())) {
            throw new RuntimeException("商户不存在");
        }

        // 检查邮箱是否被其他用户使用
        if (StringUtils.hasText(request.getEmail()) && !request.getEmail().equals(merchant.getEmail())) {
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getEmail, request.getEmail())
                   .ne(User::getId, merchantId);
            if (userMapper.selectCount(wrapper) > 0) {
                throw new RuntimeException("邮箱已被其他用户使用");
            }
        }

        // 更新商户信息
        if (StringUtils.hasText(request.getEmail())) {
            merchant.setEmail(request.getEmail());
        }
        if (StringUtils.hasText(request.getPhone())) {
            merchant.setPhone(request.getPhone());
        }
        if (StringUtils.hasText(request.getRealName())) {
            merchant.setRealName(request.getRealName());
        }
        if (StringUtils.hasText(request.getAddress())) {
            merchant.setAddress(request.getAddress());
        }
        
        merchant.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(merchant);
    }

    @Override
    public void changePassword(Long merchantId, MerchantDTO.ChangePasswordRequest request) {
        User merchant = userMapper.selectById(merchantId);
        if (merchant == null || !User.Role.MERCHANT.getCode().equals(merchant.getRole())) {
            throw new RuntimeException("商户不存在");
        }

        // 验证原密码
        if (!passwordEncoder.matches(request.getOldPassword(), merchant.getPassword())) {
            throw new RuntimeException("原密码错误");
        }

        // 更新密码
        merchant.setPassword(passwordEncoder.encode(request.getNewPassword()));
        merchant.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(merchant);
    }

    @Override
    public MerchantDTO.OrderStatsResponse getOrderStats(Long merchantId, String startDate, String endDate) {
        try {
            // 调用订单服务获取商户订单统计
            String uri = String.format("http://localhost:8082/api/orders/merchant/%d/stats", merchantId);
            if (StringUtils.hasText(startDate)) {
                uri += "?startDate=" + startDate;
                if (StringUtils.hasText(endDate)) {
                    uri += "&endDate=" + endDate;
                }
            } else if (StringUtils.hasText(endDate)) {
                uri += "?endDate=" + endDate;
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> stats = restTemplate.getForObject(uri, Map.class);

            MerchantDTO.OrderStatsResponse response = new MerchantDTO.OrderStatsResponse();
            if (stats != null) {
                response.setTotalOrders(TypeConversionUtil.toLong(stats.get("totalOrders")));
                response.setPendingOrders(TypeConversionUtil.toLong(stats.get("pendingOrders")));
                response.setCompletedOrders(TypeConversionUtil.toLong(stats.get("completedOrders")));
                response.setCancelledOrders(TypeConversionUtil.toLong(stats.get("cancelledOrders")));
                
                // 处理每日统计数据
                List<Map<String, Object>> dailyData = (List<Map<String, Object>>) stats.get("dailyStats");
                if (dailyData != null) {
                    List<MerchantDTO.DailyOrderStats> dailyStats = dailyData.stream()
                            .map(data -> {
                                MerchantDTO.DailyOrderStats dailyStat = new MerchantDTO.DailyOrderStats();
                                dailyStat.setDate((String) data.get("date"));
                                dailyStat.setOrderCount(TypeConversionUtil.toLong(data.get("orderCount")));
                                dailyStat.setRevenue(new BigDecimal(data.get("revenue").toString()));
                                return dailyStat;
                            })
                            .collect(Collectors.toList());
                    response.setDailyStats(dailyStats);
                }
            }

            return response;
        } catch (Exception e) {
            log.error("获取商户订单统计失败：merchantId={}", merchantId, e);
            // 返回默认值
            MerchantDTO.OrderStatsResponse response = new MerchantDTO.OrderStatsResponse();
            response.setTotalOrders(0L);
            response.setPendingOrders(0L);
            response.setCompletedOrders(0L);
            response.setCancelledOrders(0L);
            response.setDailyStats(new ArrayList<>());
            return response;
        }
    }

    @Override
    public MerchantDTO.FinanceStatsResponse getFinanceStats(Long merchantId, String startDate, String endDate) {
        try {
            // 调用订单服务获取商户财务统计
            String uri = String.format("http://localhost:8082/api/orders/merchant/%d/finance", merchantId);
            if (StringUtils.hasText(startDate)) {
                uri += "?startDate=" + startDate;
                if (StringUtils.hasText(endDate)) {
                    uri += "&endDate=" + endDate;
                }
            } else if (StringUtils.hasText(endDate)) {
                uri += "?endDate=" + endDate;
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> stats = restTemplate.getForObject(uri, Map.class);

            MerchantDTO.FinanceStatsResponse response = new MerchantDTO.FinanceStatsResponse();
            if (stats != null) {
                response.setTotalRevenue(new BigDecimal(stats.get("totalRevenue").toString()));
                response.setTodayRevenue(new BigDecimal(stats.get("todayRevenue").toString()));
                response.setAvgOrderAmount(new BigDecimal(stats.get("avgOrderAmount").toString()));
                
                // 处理月度收入数据
                List<Map<String, Object>> monthlyData = (List<Map<String, Object>>) stats.get("monthlyRevenues");
                if (monthlyData != null) {
                    List<MerchantDTO.MonthlyRevenue> monthlyRevenues = monthlyData.stream()
                            .map(data -> {
                                MerchantDTO.MonthlyRevenue monthlyRevenue = new MerchantDTO.MonthlyRevenue();
                                monthlyRevenue.setMonth((String) data.get("month"));
                                monthlyRevenue.setRevenue(new BigDecimal(data.get("revenue").toString()));
                                monthlyRevenue.setOrderCount(TypeConversionUtil.toLong(data.get("orderCount")));
                                return monthlyRevenue;
                            })
                            .collect(Collectors.toList());
                    response.setMonthlyRevenues(monthlyRevenues);
                }
            }

            return response;
        } catch (Exception e) {
            log.error("获取商户财务统计失败：merchantId={}", merchantId, e);
            // 返回默认值
            MerchantDTO.FinanceStatsResponse response = new MerchantDTO.FinanceStatsResponse();
            response.setTotalRevenue(BigDecimal.ZERO);
            response.setTodayRevenue(BigDecimal.ZERO);
            response.setAvgOrderAmount(BigDecimal.ZERO);
            response.setMonthlyRevenues(new ArrayList<>());
            return response;
        }
    }

    // 私有辅助方法
    private Long getPendingOrderCount(Long merchantId) {
        try {
            String url = "http://localhost:8082/api/orders/merchant/" + merchantId + "/pending/count";
            return restTemplate.getForObject(url, Long.class);
        } catch (Exception e) {
            log.warn("获取待处理订单数失败：merchantId={}", merchantId);
            return 0L;
        }
    }

    private BigDecimal getTodayRevenue(Long merchantId) {
        try {
            String url = "http://localhost:8082/api/orders/merchant/" + merchantId + "/revenue/today";
            return restTemplate.getForObject(url, BigDecimal.class);
        } catch (Exception e) {
            log.warn("获取今日营业额失败：merchantId={}", merchantId);
            return BigDecimal.ZERO;
        }
    }

    private Long getTotalProductCount(Long merchantId) {
        try {
            String url = "http://localhost:8081/api/products/merchant/" + merchantId + "/count";
            return restTemplate.getForObject(url, Long.class);
        } catch (Exception e) {
            log.warn("获取商品总数失败：merchantId={}", merchantId);
            return 0L;
        }
    }

    private Long getTotalOrderCount(Long merchantId) {
        try {
            String url = "http://localhost:8082/api/orders/merchant/" + merchantId + "/count";
            return restTemplate.getForObject(url, Long.class);
        } catch (Exception e) {
            log.warn("获取总订单数失败：merchantId={}", merchantId);
            return 0L;
        }
    }

    private List<MerchantDTO.ProductSales> getTopProducts(Long merchantId) {
        try {
            String url = "http://localhost:8081/api/products/merchant/" + merchantId + "/top?limit=5";
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> products = restTemplate.getForObject(url, List.class);

            if (products == null) {
                return new ArrayList<>();
            }

            return products.stream().map(product -> {
                MerchantDTO.ProductSales productSales = new MerchantDTO.ProductSales();
                productSales.setProductId(TypeConversionUtil.toLong(product.get("id")));
                productSales.setProductName((String) product.get("name"));
                productSales.setSales(TypeConversionUtil.toLong(product.get("sales")));
                productSales.setRevenue(new BigDecimal(product.get("revenue").toString()));
                return productSales;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("获取热销商品失败：merchantId={}", merchantId);
            return new ArrayList<>();
        }
    }

    private List<MerchantDTO.OrderTrend> getOrderTrends(Long merchantId) {
        try {
            String url = "http://localhost:8082" +
                    "/api/orders/merchant/" + merchantId + "/trends?days=7";
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> trends = restTemplate.getForObject(url, List.class);

            if (trends == null) {
                return new ArrayList<>();
            }

            return trends.stream().map(trend -> {
                MerchantDTO.OrderTrend orderTrend = new MerchantDTO.OrderTrend();
                orderTrend.setDate((String) trend.get("date"));
                orderTrend.setOrderCount(TypeConversionUtil.toLong(trend.get("orderCount")));
                orderTrend.setRevenue(new BigDecimal(trend.get("revenue").toString()));
                return orderTrend;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("获取订单趋势失败：merchantId={}", merchantId);
            return new ArrayList<>();
        }
    }
}