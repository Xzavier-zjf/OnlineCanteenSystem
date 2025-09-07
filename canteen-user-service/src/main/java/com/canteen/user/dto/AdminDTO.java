package com.canteen.user.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理员相关请求DTO
 */
public class AdminDTO {

    /**
     * 仪表板响应
     */
    @Data
    public static class DashboardResponse {
        private Integer totalUsers;          // 总用户数
        private Integer totalOrders;         // 总订单数
        private Integer totalProducts;       // 总商品数
        private String totalRevenue;         // 总收入
        private Integer todayOrders;         // 今日订单数
        private String todayRevenue;         // 今日收入
        private Integer activeUsers;         // 活跃用户数
        private Integer pendingOrders;       // 待处理订单数
        private List<RecentOrder> recentOrders; // 最近订单
        private List<HotProduct> hotProducts;   // 热门商品
        
        @Data
        public static class RecentOrder {
            private Long orderId;
            private String orderNo;
            private String userName;
            private String totalAmount;
            private String status;
            private LocalDateTime createTime;
        }
        
        @Data
        public static class HotProduct {
            private Long productId;
            private String productName;
            private Integer sales;
            private String revenue;
            private String imageUrl;
        }
    }

    /**
     * 系统设置响应
     */
    @Data
    public static class SystemSettingsResponse {
        private String systemName;           // 系统名称
        private String systemVersion;        // 系统版本
        private String systemDescription;    // 系统描述
        private String contactEmail;         // 联系邮箱
        private String contactPhone;         // 联系电话
        private Boolean maintenanceMode;     // 维护模式
        private Integer maxUsers;            // 最大用户数
        private Integer sessionTimeout;      // 会话超时时间(分钟)
        private Boolean enableRegistration;  // 是否允许注册
        private Boolean enableEmailVerification; // 是否启用邮箱验证
    }

    /**
     * 更新系统设置请求
     */
    @Data
    public static class UpdateSystemSettingsRequest {
        private String systemName;
        private String systemVersion;
        private String systemDescription;
        private String contactEmail;
        private String contactPhone;
        private Boolean maintenanceMode;
        private Integer maxUsers;
        private Integer sessionTimeout;
        private Boolean enableRegistration;
        private Boolean enableEmailVerification;
    }

    /**
     * 系统统计数据响应
     */
    @Data
    public static class SystemStatsResponse {
        private Integer totalUsers;          // 总用户数
        private Integer activeUsers;         // 活跃用户数
        private Integer totalOrders;         // 总订单数
        private Integer todayOrders;         // 今日订单数
        private String totalRevenue;         // 总收入
        private String todayRevenue;         // 今日收入
        private Integer totalProducts;       // 总商品数
        private Integer activeProducts;      // 上架商品数
        private Double systemLoad;           // 系统负载
        private String memoryUsage;          // 内存使用率
        private String diskUsage;            // 磁盘使用率
        private LocalDateTime lastUpdateTime; // 最后更新时间
    }

    /**
     * 备份响应
     */
    @Data
    public static class BackupResponse {
        private String backupId;             // 备份ID
        private String backupPath;           // 备份路径
        private String backupSize;           // 备份大小
        private LocalDateTime backupTime;    // 备份时间
        private String status;               // 备份状态
    }

    /**
     * 系统日志响应
     */
    @Data
    public static class SystemLogsResponse {
        private List<SystemLog> logs;
        private Integer total;
        private Integer page;
        private Integer size;

        @Data
        public static class SystemLog {
            private Long id;
            private String level;            // 日志级别: INFO, WARN, ERROR
            private String message;          // 日志消息
            private String module;           // 模块名称
            private String userId;           // 用户ID
            private String ip;               // IP地址
            private LocalDateTime createTime; // 创建时间
        }
    }
}