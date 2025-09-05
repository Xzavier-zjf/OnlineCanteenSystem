package com.canteen.user.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商户相关DTO
 */
public class MerchantDTO {

    /**
     * 商户登录请求
     */
    @Data
    public static class LoginRequest {
        @NotBlank(message = "用户名不能为空")
        private String username;
        
        @NotBlank(message = "密码不能为空")
        private String password;
    }

    /**
     * 商户登录响应
     */
    @Data
    public static class LoginResponse {
        private Long id;
        private String username;
        private String token;
        private String role;
        private Integer status;
        private String shopName;
    }

    /**
     * 商户注册请求
     */
    @Data
    public static class RegisterRequest {
        @NotBlank(message = "用户名不能为空")
        @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
        private String username;
        
        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 100, message = "密码长度必须在6-100个字符之间")
        private String password;
        
        @Email(message = "邮箱格式不正确")
        private String email;
        
        private String phone;
        
        @NotBlank(message = "真实姓名不能为空")
        private String realName;
        
        @NotBlank(message = "店铺名称不能为空")
        private String shopName;
        
        private String address;
    }

    /**
     * 商户仪表板数据响应
     */
    @Data
    public static class DashboardResponse {
        private Long pendingOrders;        // 待处理订单数
        private BigDecimal todayRevenue;   // 今日营业额
        private Long totalProducts;        // 商品总数
        private Long totalOrders;          // 总订单数
        private List<ProductSales> topProducts;  // 热销商品
        private List<OrderTrend> orderTrends;    // 订单趋势
    }

    /**
     * 商品销量
     */
    @Data
    public static class ProductSales {
        private Long productId;
        private String productName;
        private Long sales;
        private BigDecimal revenue;
    }

    /**
     * 订单趋势
     */
    @Data
    public static class OrderTrend {
        private String date;
        private Long orderCount;
        private BigDecimal revenue;
    }

    /**
     * 商户信息
     */
    @Data
    public static class MerchantInfo {
        private Long id;
        private String username;
        private String email;
        private String phone;
        private String realName;
        private String shopName;
        private String address;
        private Integer status;
        private LocalDateTime createTime;
    }

    /**
     * 更新商户信息请求
     */
    @Data
    public static class UpdateInfoRequest {
        @Email(message = "邮箱格式不正确")
        private String email;
        
        private String phone;
        
        private String realName;
        
        private String shopName;
        
        private String address;
    }

    /**
     * 修改密码请求
     */
    @Data
    public static class ChangePasswordRequest {
        @NotBlank(message = "原密码不能为空")
        private String oldPassword;
        
        @NotBlank(message = "新密码不能为空")
        @Size(min = 6, max = 100, message = "新密码长度必须在6-100个字符之间")
        private String newPassword;
    }

    /**
     * 订单统计响应
     */
    @Data
    public static class OrderStatsResponse {
        private Long totalOrders;          // 总订单数
        private Long pendingOrders;        // 待处理订单
        private Long completedOrders;      // 已完成订单
        private Long cancelledOrders;      // 已取消订单
        private List<DailyOrderStats> dailyStats;  // 每日统计
    }

    /**
     * 每日订单统计
     */
    @Data
    public static class DailyOrderStats {
        private String date;
        private Long orderCount;
        private BigDecimal revenue;
    }

    /**
     * 财务统计响应
     */
    @Data
    public static class FinanceStatsResponse {
        private BigDecimal totalRevenue;   // 总收入
        private BigDecimal todayRevenue;   // 今日收入
        private BigDecimal avgOrderAmount; // 平均订单金额
        private List<MonthlyRevenue> monthlyRevenues;  // 月度收入
    }

    /**
     * 月度收入
     */
    @Data
    public static class MonthlyRevenue {
        private String month;
        private BigDecimal revenue;
        private Long orderCount;
    }
}