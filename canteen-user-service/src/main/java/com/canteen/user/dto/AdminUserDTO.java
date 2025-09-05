package com.canteen.user.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理员用户数据传输对象
 */
@Data
public class AdminUserDTO {
    
    private Long id;
    
    private String username;
    
    private String email;
    
    private String phone;
    
    private String role;
    
    private String roleDesc;
    
    private String status;
    
    private String statusDesc;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
    
    private LocalDateTime lastLoginTime;
    
    // 商户相关信息
    private String merchantName;
    
    private String merchantDescription;
    
    private String merchantAddress;
    
    private String merchantPhone;
    
    private String approvalStatus;
    
    private String approvalReason;
    
    // 统计信息
    private Long totalOrders;
    
    private java.math.BigDecimal totalSpent;
}