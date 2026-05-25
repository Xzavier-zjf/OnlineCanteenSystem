package com.canteen.user.service;

import com.canteen.user.dto.MerchantDTO;

/**
 * 商户服务接口
 */
public interface MerchantService {

    /**
     * 商户登录
     */
    MerchantDTO.LoginResponse login(MerchantDTO.LoginRequest request);

    /**
     * 商户注册
     */
    void register(MerchantDTO.RegisterRequest request);

    /**
     * 获取商户仪表板数据
     */
    MerchantDTO.DashboardResponse getDashboard(Long merchantId, String authorization);

    /**
     * 获取商户信息
     */
    MerchantDTO.MerchantInfo getMerchantInfo(Long merchantId);

    /**
     * 更新商户信息
     */
    void updateMerchantInfo(Long merchantId, MerchantDTO.UpdateInfoRequest request);

    /**
     * 修改密码
     */
    void changePassword(Long merchantId, MerchantDTO.ChangePasswordRequest request);

    /**
     * 获取订单统计
     */
    MerchantDTO.OrderStatsResponse getOrderStats(Long merchantId, String startDate, String endDate, String authorization);

    /**
     * 获取财务统计
     */
    MerchantDTO.FinanceStatsResponse getFinanceStats(Long merchantId, String startDate, String endDate, String authorization);

    /**
     * 获取热门商品
     */
    java.util.List<java.util.Map<String, Object>> getTopProducts(Integer limit);

    /**
     * 获取收入明细
     */
    java.util.Map<String, Object> getRevenueDetails(Long merchantId, String startDate, String endDate, String authorization);

    /**
     * 导出财务报表
     */
    byte[] exportFinancialReport(Long merchantId, String startDate, String endDate, String authorization);

    /**
     * 获取店铺设置
     */
    java.util.Map<String, Object> getShopSettings(Long merchantId);

    /**
     * 更新店铺设置
     */
    void updateShopSettings(Long merchantId, java.util.Map<String, Object> settings);
}
