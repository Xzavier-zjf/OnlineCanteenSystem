package com.canteen.order.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 管理员订单服务接口
 */
public interface AdminOrderService {

    /**
     * 获取订单总数
     */
    Long getTotalOrderCount();

    /**
     * 获取今日销售额
     */
    BigDecimal getTodaySales();

    /**
     * 获取总销售额
     */
    BigDecimal getTotalSales();

    /**
     * 获取订单状态统计
     */
    List<Map<String, Object>> getOrderStatusStats();

    /**
     * 获取销售统计
     */
    List<Map<String, Object>> getSalesStats(Integer days);

    /**
     * 获取用户订单统计
     */
    Map<String, Object> getUserOrderStats(Long userId);

    /**
     * 获取管理员订单列表
     */
    Map<String, Object> getAdminOrderList(Integer page, Integer size, String status, String startDate, String endDate);

    /**
     * 获取所有订单
     */
    Map<String, Object> getAllOrders(Integer page, Integer size, String status);

    /**
     * 获取订单统计
     */
    Map<String, Object> getOrderStatistics();

    /**
     * 获取销售统计
     */
    Map<String, Object> getSalesStatistics(String startDate, String endDate);
}
