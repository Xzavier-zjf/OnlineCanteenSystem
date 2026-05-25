package com.canteen.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.canteen.common.utils.JwtUtils;
import com.canteen.user.dto.MerchantDTO;
import com.canteen.user.entity.MerchantSettings;
import com.canteen.user.entity.User;
import com.canteen.user.mapper.MerchantSettingsMapper;
import com.canteen.user.mapper.UserMapper;
import com.canteen.user.service.MerchantService;
import com.canteen.user.utils.TypeConversionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
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
    private final MerchantSettingsMapper merchantSettingsMapper;
    private final PasswordEncoder passwordEncoder;
    private final RestTemplate restTemplate = new RestTemplate();
    private static final String PRODUCT_SERVICE_URL = "http://localhost:8082";
    private static final String ORDER_SERVICE_URL = "http://localhost:8083";

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
    public MerchantDTO.DashboardResponse getDashboard(Long merchantId, String authorization) {
        MerchantDTO.DashboardResponse dashboard = new MerchantDTO.DashboardResponse();

        dashboard.setPendingOrders(getPendingOrderCount(merchantId, authorization));
        dashboard.setTodayRevenue(getTodayRevenue(merchantId, authorization));
        dashboard.setTotalProducts(getTotalProductCount(merchantId, authorization));
        dashboard.setTotalOrders(getTotalOrderCount(merchantId, authorization));
        dashboard.setTopProducts(getTopProducts(merchantId, authorization));
        dashboard.setOrderTrends(getOrderTrends(merchantId, authorization));

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
    public MerchantDTO.OrderStatsResponse getOrderStats(Long merchantId, String startDate, String endDate, String authorization) {
        String uri = buildMerchantOrderUrl(merchantId, "/stats", startDate, endDate).toUriString();

        @SuppressWarnings("unchecked")
        Map<String, Object> stats = getWithAuth(uri, authorization, Map.class);

        MerchantDTO.OrderStatsResponse response = new MerchantDTO.OrderStatsResponse();
        response.setTotalOrders(TypeConversionUtil.toLong(stats.get("totalOrders")));
        response.setPendingOrders(TypeConversionUtil.toLong(stats.get("pendingOrders")));
        response.setCompletedOrders(TypeConversionUtil.toLong(stats.get("completedOrders")));
        response.setCancelledOrders(TypeConversionUtil.toLong(stats.get("cancelledOrders")));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> dailyData = (List<Map<String, Object>>) stats.get("dailyStats");
        if (dailyData != null) {
            List<MerchantDTO.DailyOrderStats> dailyStats = dailyData.stream()
                    .map(data -> {
                        MerchantDTO.DailyOrderStats dailyStat = new MerchantDTO.DailyOrderStats();
                        dailyStat.setDate((String) data.get("date"));
                        dailyStat.setOrderCount(TypeConversionUtil.toLong(data.get("orderCount")));
                        dailyStat.setRevenue(toBigDecimal(data.get("revenue")));
                        return dailyStat;
                    })
                    .collect(Collectors.toList());
            response.setDailyStats(dailyStats);
        } else {
            response.setDailyStats(new ArrayList<>());
        }

        return response;
    }

    @Override
    public MerchantDTO.FinanceStatsResponse getFinanceStats(Long merchantId, String startDate, String endDate, String authorization) {
        Map<String, Object> stats = getFinanceStatsMap(merchantId, startDate, endDate, authorization);

        MerchantDTO.FinanceStatsResponse response = new MerchantDTO.FinanceStatsResponse();
        response.setTotalRevenue(toBigDecimal(stats.get("totalRevenue")));
        response.setTodayRevenue(toBigDecimal(stats.get("todayRevenue")));
        response.setAvgOrderAmount(toBigDecimal(stats.get("avgOrderAmount")));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> monthlyData = (List<Map<String, Object>>) stats.get("monthlyRevenues");
        if (monthlyData != null) {
            List<MerchantDTO.MonthlyRevenue> monthlyRevenues = monthlyData.stream()
                    .map(data -> {
                        MerchantDTO.MonthlyRevenue monthlyRevenue = new MerchantDTO.MonthlyRevenue();
                        monthlyRevenue.setMonth((String) data.get("month"));
                        monthlyRevenue.setRevenue(toBigDecimal(data.get("revenue")));
                        monthlyRevenue.setOrderCount(TypeConversionUtil.toLong(data.get("orderCount")));
                        return monthlyRevenue;
                    })
                    .collect(Collectors.toList());
            response.setMonthlyRevenues(monthlyRevenues);
        } else {
            response.setMonthlyRevenues(new ArrayList<>());
        }

        return response;
    }

    // 私有辅助方法
    private Long getPendingOrderCount(Long merchantId, String authorization) {
        String url = ORDER_SERVICE_URL + "/api/orders/merchant/" + merchantId + "/pending/count";
        return getWithAuth(url, authorization, Long.class);
    }

    private BigDecimal getTodayRevenue(Long merchantId, String authorization) {
        String url = ORDER_SERVICE_URL + "/api/orders/merchant/" + merchantId + "/revenue/today";
        return getWithAuth(url, authorization, BigDecimal.class);
    }

    private Long getTotalProductCount(Long merchantId, String authorization) {
        String url = PRODUCT_SERVICE_URL + "/api/products/merchant/" + merchantId + "/count";
        return getWithAuth(url, authorization, Long.class);
    }

    private Long getTotalOrderCount(Long merchantId, String authorization) {
        String url = ORDER_SERVICE_URL + "/api/orders/merchant/" + merchantId + "/count";
        return getWithAuth(url, authorization, Long.class);
    }

    private List<MerchantDTO.ProductSales> getTopProducts(Long merchantId, String authorization) {
        String url = PRODUCT_SERVICE_URL + "/api/products/merchant/" + merchantId + "/top?limit=5";
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> products = getWithAuth(url, authorization, List.class);

        if (products == null) {
            return new ArrayList<>();
        }

        return products.stream().map(product -> {
            MerchantDTO.ProductSales productSales = new MerchantDTO.ProductSales();
            productSales.setProductId(TypeConversionUtil.toLong(product.get("id")));
            productSales.setProductName((String) product.get("name"));
            productSales.setSales(TypeConversionUtil.toLong(product.get("sales")));
            productSales.setRevenue(toBigDecimal(product.get("revenue")));
            return productSales;
        }).collect(Collectors.toList());
    }

    private List<MerchantDTO.OrderTrend> getOrderTrends(Long merchantId, String authorization) {
        String url = ORDER_SERVICE_URL + "/api/orders/merchant/" + merchantId + "/trends?days=7";
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> trends = getWithAuth(url, authorization, List.class);

        if (trends == null) {
            return new ArrayList<>();
        }

        return trends.stream().map(trend -> {
            MerchantDTO.OrderTrend orderTrend = new MerchantDTO.OrderTrend();
            orderTrend.setDate((String) trend.get("date"));
            orderTrend.setOrderCount(TypeConversionUtil.toLong(trend.get("orderCount")));
            orderTrend.setRevenue(toBigDecimal(trend.get("revenue")));
            return orderTrend;
        }).collect(Collectors.toList());
    }

    @Override
    public java.util.List<java.util.Map<String, Object>> getTopProducts(Integer limit) {
        String url = PRODUCT_SERVICE_URL + "/api/products/top?limit=" + limit;
        @SuppressWarnings("unchecked")
        java.util.List<java.util.Map<String, Object>> products = restTemplate.getForObject(url, java.util.List.class);
        return products != null ? products : new ArrayList<>();
    }

    @Override
    public java.util.Map<String, Object> getRevenueDetails(Long merchantId, String startDate, String endDate, String authorization) {
        Map<String, Object> stats = getFinanceStatsMap(merchantId, startDate, endDate, authorization);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> monthlyRevenues = (List<Map<String, Object>>) stats.get("monthlyRevenues");
        List<Map<String, Object>> revenueList = new ArrayList<>();
        if (monthlyRevenues != null) {
            for (Map<String, Object> revenue : monthlyRevenues) {
                java.util.Map<String, Object> item = new java.util.HashMap<>();
                item.put("date", revenue.get("month"));
                item.put("month", revenue.get("month"));
                item.put("orderCount", TypeConversionUtil.toLong(revenue.get("orderCount")));
                BigDecimal amount = toBigDecimal(revenue.get("revenue"));
                Long orderCount = TypeConversionUtil.toLong(revenue.get("orderCount"));
                item.put("revenue", amount);
                item.put("avgOrderValue", orderCount == null || orderCount == 0
                        ? BigDecimal.ZERO
                        : amount.divide(BigDecimal.valueOf(orderCount), 2, java.math.RoundingMode.HALF_UP));
                revenueList.add(item);
            }
        }

        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("list", revenueList);
        result.put("total", revenueList.size());
        return result;
    }

    @Override
    public byte[] exportFinancialReport(Long merchantId, String startDate, String endDate, String authorization) {
        java.util.Map<String, Object> details = getRevenueDetails(merchantId, startDate, endDate, authorization);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> revenueList = (List<Map<String, Object>>) details.getOrDefault("list", new ArrayList<>());

        StringBuilder csv = new StringBuilder();
        csv.append('\uFEFF');
        csv.append("日期,订单数,营收,客单价\n");
        for (Map<String, Object> item : revenueList) {
            csv.append(csvValue(item.get("date"))).append(',')
                    .append(csvValue(item.get("orderCount"))).append(',')
                    .append(csvValue(item.get("revenue"))).append(',')
                    .append(csvValue(item.get("avgOrderValue"))).append('\n');
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public java.util.Map<String, Object> getShopSettings(Long merchantId) {
        try {
            return getShopSettingsFromDatabase(merchantId);
        } catch (Exception e) {
            log.error("获取店铺设置失败：merchantId={}", merchantId, e);
            return getDefaultShopSettings();
        }
    }

    @Override
    public void updateShopSettings(Long merchantId, java.util.Map<String, Object> settings) {
        try {
            QueryWrapper<MerchantSettings> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", merchantId);
            MerchantSettings merchantSettings = merchantSettingsMapper.selectOne(queryWrapper);
            
            if (merchantSettings == null) {
                // 创建新的设置记录
                merchantSettings = new MerchantSettings();
                merchantSettings.setUserId(merchantId);
            }
            
            // 更新设置字段
            if (settings.containsKey("shopName")) {
                merchantSettings.setShopName((String) settings.get("shopName"));
            }
            if (settings.containsKey("shopDescription")) {
                merchantSettings.setShopDescription((String) settings.get("shopDescription"));
            }
            if (settings.containsKey("shopLogo")) {
                merchantSettings.setShopLogo((String) settings.get("shopLogo"));
            }
            if (settings.containsKey("contactPhone")) {
                merchantSettings.setContactPhone((String) settings.get("contactPhone"));
            }
            if (settings.containsKey("shopAddress")) {
                merchantSettings.setShopAddress((String) settings.get("shopAddress"));
            }
            if (settings.containsKey("deliveryFee")) {
                Object deliveryFee = settings.get("deliveryFee");
                if (deliveryFee instanceof Number) {
                    merchantSettings.setDeliveryFee(new java.math.BigDecimal(deliveryFee.toString()));
                }
            }
            if (settings.containsKey("minOrderAmount")) {
                Object minOrderAmount = settings.get("minOrderAmount");
                if (minOrderAmount instanceof Number) {
                    merchantSettings.setMinOrderAmount(new java.math.BigDecimal(minOrderAmount.toString()));
                }
            }
            if (settings.containsKey("autoAcceptOrder")) {
                merchantSettings.setAutoAcceptOrder((Boolean) settings.get("autoAcceptOrder"));
            }
            if (settings.containsKey("businessHours")) {
                Object businessHours = settings.get("businessHours");
                if (businessHours instanceof String) {
                    merchantSettings.setBusinessHours((String) businessHours);
                } else if (businessHours instanceof java.util.Map) {
                    // 将Map转换为JSON字符串
                    ObjectMapper objectMapper = new ObjectMapper();
                    merchantSettings.setBusinessHours(objectMapper.writeValueAsString(businessHours));
                }
            }
            
            merchantSettings.setUpdateTime(LocalDateTime.now());
            
            if (merchantSettings.getId() == null) {
                merchantSettingsMapper.insert(merchantSettings);
            } else {
                merchantSettingsMapper.updateById(merchantSettings);
            }
            
            log.info("更新店铺设置成功：merchantId={}", merchantId);
        } catch (Exception e) {
            log.error("更新店铺设置失败：{}", settings, e);
            throw new RuntimeException("更新店铺设置失败: " + e.getMessage());
        }
    }

    private java.util.Map<String, Object> getShopSettingsFromDatabase(Long merchantId) {
        try {
            QueryWrapper<MerchantSettings> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", merchantId);
            MerchantSettings settings = merchantSettingsMapper.selectOne(queryWrapper);
            
            java.util.Map<String, Object> result = new java.util.HashMap<>();
            if (settings != null) {
                result.put("shopName", settings.getShopName());
                result.put("shopDescription", settings.getShopDescription());
                result.put("shopLogo", settings.getShopLogo());
                result.put("contactPhone", settings.getContactPhone());
                result.put("shopAddress", settings.getShopAddress());
                result.put("deliveryFee", settings.getDeliveryFee());
                result.put("minOrderAmount", settings.getMinOrderAmount());
                result.put("autoAcceptOrder", settings.getAutoAcceptOrder());
                result.put("businessHours", settings.getBusinessHours());
            } else {
                // 如果没有设置记录，创建默认设置
                settings = createDefaultMerchantSettings(merchantId);
                merchantSettingsMapper.insert(settings);
                result = convertMerchantSettingsToMap(settings);
            }
            return result;
        } catch (Exception e) {
            log.error("从数据库获取店铺设置失败: merchantId={}", merchantId, e);
            return getDefaultShopSettings();
        }
    }

    private java.util.Map<String, Object> getDefaultShopSettings() {
        java.util.Map<String, Object> settings = new java.util.HashMap<>();
        settings.put("shopName", "我的店铺");
        settings.put("shopDescription", "欢迎来到我的店铺");
        settings.put("shopLogo", "/images/default-shop-logo.png");
        settings.put("contactPhone", "");
        settings.put("shopAddress", "");
        settings.put("deliveryFee", 2.00);
        settings.put("minOrderAmount", 20.00);
        settings.put("autoAcceptOrder", true);
        settings.put("businessHours", "{\"monday\": {\"open\": \"08:00\", \"close\": \"20:00\"}, \"tuesday\": {\"open\": \"08:00\", \"close\": \"20:00\"}, \"wednesday\": {\"open\": \"08:00\", \"close\": \"20:00\"}, \"thursday\": {\"open\": \"08:00\", \"close\": \"20:00\"}, \"friday\": {\"open\": \"08:00\", \"close\": \"20:00\"}, \"saturday\": {\"open\": \"09:00\", \"close\": \"21:00\"}, \"sunday\": {\"open\": \"09:00\", \"close\": \"21:00\"}}");
        return settings;
    }
    
    private MerchantSettings createDefaultMerchantSettings(Long merchantId) {
        MerchantSettings settings = new MerchantSettings();
        settings.setUserId(merchantId);
        settings.setShopName("我的店铺");
        settings.setShopDescription("欢迎来到我的店铺");
        settings.setShopLogo("/images/default-shop-logo.png");
        settings.setContactPhone("");
        settings.setShopAddress("");
        settings.setDeliveryFee(new java.math.BigDecimal("2.00"));
        settings.setMinOrderAmount(new java.math.BigDecimal("20.00"));
        settings.setAutoAcceptOrder(true);
        settings.setBusinessHours("{\"monday\": {\"open\": \"08:00\", \"close\": \"20:00\"}, \"tuesday\": {\"open\": \"08:00\", \"close\": \"20:00\"}, \"wednesday\": {\"open\": \"08:00\", \"close\": \"20:00\"}, \"thursday\": {\"open\": \"08:00\", \"close\": \"20:00\"}, \"friday\": {\"open\": \"08:00\", \"close\": \"20:00\"}, \"saturday\": {\"open\": \"09:00\", \"close\": \"21:00\"}, \"sunday\": {\"open\": \"09:00\", \"close\": \"21:00\"}}");
        return settings;
    }
    
    private java.util.Map<String, Object> convertMerchantSettingsToMap(MerchantSettings settings) {
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("shopName", settings.getShopName());
        result.put("shopDescription", settings.getShopDescription());
        result.put("shopLogo", settings.getShopLogo());
        result.put("contactPhone", settings.getContactPhone());
        result.put("shopAddress", settings.getShopAddress());
        result.put("deliveryFee", settings.getDeliveryFee());
        result.put("minOrderAmount", settings.getMinOrderAmount());
        result.put("autoAcceptOrder", settings.getAutoAcceptOrder());
        result.put("businessHours", settings.getBusinessHours());
        return result;
    }

    private Map<String, Object> getFinanceStatsMap(Long merchantId, String startDate, String endDate, String authorization) {
        String uri = buildMerchantOrderUrl(merchantId, "/finance", startDate, endDate).toUriString();

        @SuppressWarnings("unchecked")
        Map<String, Object> stats = getWithAuth(uri, authorization, Map.class);
        return stats;
    }

    private UriComponentsBuilder buildMerchantOrderUrl(Long merchantId, String path, String startDate, String endDate) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(ORDER_SERVICE_URL + "/api/orders/merchant/" + merchantId + path);
        if (StringUtils.hasText(startDate)) {
            builder.queryParam("startDate", startDate);
        }
        if (StringUtils.hasText(endDate)) {
            builder.queryParam("endDate", endDate);
        }
        return builder;
    }

    private <T> T getWithAuth(String url, String authorization, Class<T> responseType) {
        HttpHeaders headers = new HttpHeaders();
        if (StringUtils.hasText(authorization)) {
            headers.set("Authorization", authorization);
        }
        ResponseEntity<T> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                responseType
        );
        return response.getBody();
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(value.toString());
    }

    private String csvValue(Object value) {
        String text = value == null ? "" : value.toString();
        if (text.contains(",") || text.contains("\"") || text.contains("\n")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }
}
