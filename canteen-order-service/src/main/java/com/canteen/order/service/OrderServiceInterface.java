package com.canteen.order.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.canteen.order.controller.OrderController;
import com.canteen.order.entity.Order;
import com.canteen.order.entity.OrderItem;

import java.util.List;

/**
 * 订单服务接口
 */
public interface OrderServiceInterface {

    /**
     * 创建订单
     */
    Order createOrder(Long userId, List<OrderItem> items, String remark);

    /**
     * 获取用户订单列表
     */
    Page<Order> getUserOrders(Long userId, Long current, Long size, String status);

    /**
     * 根据ID获取订单
     */
    Order getOrderById(Long id);

    /**
     * 获取订单项列表
     */
    List<OrderItem> getOrderItems(Long orderId);

    /**
     * 更新订单状态
     */
    boolean updateOrderStatus(Long orderId, String status);

    /**
     * 支付订单
     */
    boolean payOrder(Long orderId);

    /**
     * 取消订单
     */
    boolean cancelOrder(Long orderId);

    /**
     * 获取用户订单统计数据
     */
    OrderController.UserOrderStats getUserOrderStats(Long userId);
}