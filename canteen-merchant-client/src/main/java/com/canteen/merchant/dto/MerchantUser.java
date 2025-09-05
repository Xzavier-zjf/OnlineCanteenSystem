package com.canteen.merchant.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商户用户信息
 */
@Data
public class MerchantUser {
    
    private Long id;
    
    private String username;
    
    private String email;
    
    private String phone;
    
    private String role;
    
    private String status;
    
    private LocalDateTime createTime;
    
    private LocalDateTime lastLoginTime;
    
    // 商户相关信息
    private String merchantName;
    
    private String merchantDescription;
    
    private String merchantAddress;
    
    private String merchantPhone;
    
    private String merchantLogo;
    
    // 认证token
    private String token;
}