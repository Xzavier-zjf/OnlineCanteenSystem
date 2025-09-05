package com.canteen.user.service;

import com.canteen.user.dto.AdminUserDTO;

import java.util.Map;

/**
 * 管理员用户服务接口
 */
public interface AdminUserService {

    /**
     * 获取用户列表（分页）
     */
    Map<String, Object> getUserList(Integer page, Integer size, String keyword, String role, String status);

    /**
     * 获取用户详情
     */
    AdminUserDTO getUserDetail(Long userId);

    /**
     * 更新用户状态
     */
    void updateUserStatus(Long userId, String status);

    /**
     * 重置用户密码
     */
    String resetUserPassword(Long userId);

    /**
     * 获取用户统计信息
     */
    Map<String, Object> getUserStatistics();

    /**
     * 审核商户注册
     */
    void approveMerchant(Long userId, Boolean approved, String reason);

    /**
     * 获取待审核商户列表
     */
    Map<String, Object> getPendingMerchants(Integer page, Integer size);
}