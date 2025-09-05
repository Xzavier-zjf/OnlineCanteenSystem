package com.canteen.merchant.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 商户订单服务接口
 */
public interface MerchantOrderService {
    
    /**
     * 获取待处理订单数量
     */
    Long getPendingOrderCount(Long merchantId);
    
    /**
     * 获取今日营业额
     */
    BigDecimal getTodayRevenue(Long merchantId);
    
    /**
     * 获取订单总数
     */
    Long getTotalOrderCount(Long merchantId);
    
    /**
     * 获取订单列表
     */
    List<Map<String, Object>> getOrderList(Long merchantId, Integer page, Integer size, String status);
    
    /**
     * 接受订单
     */
    void acceptOrder(Long orderId, Long merchantId);
    
    /**
     * 拒绝订单
     */
    void rejectOrder(Long orderId, Long merchantId, String reason);
    
    /**
     * 更新订单状态
     */
    void updateOrderStatus(Long orderId, Long merchantId, String status);
    
    /**
     * 获取订单详情
     */
    Map<String, Object> getOrderDetail(Long orderId, Long merchantId);
}