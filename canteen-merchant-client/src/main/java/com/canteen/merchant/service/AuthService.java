package com.canteen.merchant.service;

import com.canteen.merchant.dto.MerchantUser;

/**
 * 认证服务接口
 */
public interface AuthService {
    
    /**
     * 用户登录
     */
    boolean login(String username, String password);
    
    /**
     * 获取当前用户信息
     */
    MerchantUser getCurrentUser();
    
    /**
     * 退出登录
     */
    void logout();
    
    /**
     * 检查是否已登录
     */
    boolean isLoggedIn();
}