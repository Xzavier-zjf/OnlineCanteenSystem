package com.canteen.order.service;

import com.canteen.order.entity.Order;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 商户订单服务接口
 */
public interface MerchantOrderService {

    /**
     * 获取商户待处理订单数
     */
    Long getPendingOrderCount(Long merchantId);

    /**
     * 获取商户今日营业额
     */
    BigDecimal getTodayRevenue(Long merchantId);

    /**
     * 获取商户订单总数
     */
    Long getTotalOrderCount(Long merchantId);

    /**
     * 获取商户订单趋势
     */
    List<Map<String, Object>> getOrderTrends(Long merchantId, Integer days);

    /**
     * 获取商户订单统计
     */
    Map<String, Object> getMerchantOrderStats(Long merchantId, String startDate, String endDate);

    /**
     * 获取商户财务统计
     */
    Map<String, Object> getMerchantFinanceStats(Long merchantId, String startDate, String endDate);

    /**
     * 获取商户订单列表
     */
    Map<String, Object> getMerchantOrderList(Long merchantId, Integer page, Integer size, String status);

    /**
     * 商户接单
     */
    void acceptOrder(Long orderId, Long merchantId);

    /**
     * 商户拒单
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
    
    /**
     * 获取商户的待处理订单列表
     */
    List<Order> getPendingOrders(Long merchantId);
}