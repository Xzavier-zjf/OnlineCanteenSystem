package com.canteen.user.service;

import com.canteen.user.dto.AdminDTO;

/**
 * 管理员服务接口
 */
public interface AdminService {

    /**
     * 管理员登录
     */
    AdminDTO.LoginResponse login(AdminDTO.LoginRequest request);

    /**
     * 获取仪表板数据
     */
    AdminDTO.DashboardResponse getDashboard();

    /**
     * 获取用户列表
     */
    AdminDTO.PageResponse<AdminDTO.UserInfo> getUsers(Integer page, Integer size, String keyword, String role);

    /**
     * 更新用户状态
     */
    void updateUserStatus(Long userId, Integer status);

    /**
     * 重置用户密码
     */
    String resetUserPassword(Long userId);

    /**
     * 获取商户列表
     */
    AdminDTO.PageResponse<AdminDTO.MerchantInfo> getMerchants(Integer page, Integer size, String keyword);

    /**
     * 审核商户
     */
    void approveMerchant(Long merchantId, Boolean approved, String reason);
}