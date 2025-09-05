package com.canteen.user.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理员相关DTO
 */
public class AdminDTO {

    /**
     * 管理员登录请求
     */
    @Data
    public static class LoginRequest {
        @NotBlank(message = "用户名不能为空")
        private String username;
        
        @NotBlank(message = "密码不能为空")
        private String password;
    }

    /**
     * 管理员登录响应
     */
    @Data
    public static class LoginResponse {
        private Long id;
        private String username;
        private String token;
        private String role;
    }

    /**
     * 仪表板数据响应
     */
    @Data
    public static class DashboardResponse {
        private Long totalUsers;           // 总用户数
        private Long totalMerchants;       // 总商户数
        private Long totalOrders;          // 总订单数
        private Long totalProducts;        // 总商品数
        private BigDecimal todaySales;     // 今日销售额
        private BigDecimal totalSales;     // 总销售额
        private List<OrderStats> orderStats;  // 订单统计
        private List<SalesStats> salesStats;  // 销售统计
    }

    /**
     * 订单统计
     */
    @Data
    public static class OrderStats {
        private String status;
        private Long count;
    }

    /**
     * 销售统计
     */
    @Data
    public static class SalesStats {
        private String date;
        private BigDecimal amount;
        private Long orderCount;
    }

    /**
     * 分页响应
     */
    @Data
    public static class PageResponse<T> {
        private List<T> records;
        private Long total;
        private Integer page;
        private Integer size;
        private Integer pages;
    }

    /**
     * 用户信息
     */
    @Data
    public static class UserInfo {
        private Long id;
        private String username;
        private String email;
        private String phone;
        private String realName;
        private String college;
        private String role;
        private Integer status;
        private LocalDateTime createTime;
        private Long totalOrders;
        private BigDecimal totalSpent;
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
        private Integer status;
        private LocalDateTime createTime;
        private Long totalProducts;
        private Long totalOrders;
        private BigDecimal totalSales;
    }

    /**
     * 更新状态请求
     */
    @Data
    public static class UpdateStatusRequest {
        @NotNull(message = "状态不能为空")
        private Integer status;
    }

    /**
     * 审核请求
     */
    @Data
    public static class ApproveRequest {
        @NotNull(message = "审核结果不能为空")
        private Boolean approved;
        private String reason;
    }
}