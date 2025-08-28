package com.canteen.order.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.canteen.order.controller.OrderController;
import com.canteen.order.entity.Order;
import com.canteen.order.entity.OrderItem;
import com.canteen.order.mapper.OrderMapper;
import com.canteen.order.mapper.OrderItemMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 订单服务类
 */
@Service
public class OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    /**
     * 创建订单
     */
    @Transactional
    public Order createOrder(Long userId, List<OrderItem> items, String remark) {
        // 计算订单总金额
        BigDecimal totalAmount = items.stream()
                .map(item -> item.getPrice().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 创建订单
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setStatus(Order.Status.PENDING.getCode());
        order.setRemark(remark);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());

        orderMapper.insert(order);

        // 创建订单详情
        for (OrderItem item : items) {
            item.setOrderId(order.getId());
            item.setSubtotal(item.getPrice().multiply(new BigDecimal(item.getQuantity())));
            orderItemMapper.insert(item);
        }

        return order;
    }

    /**
     * 获取用户订单列表
     */
    public Page<Order> getUserOrders(Long userId, Long current, Long size, String status) {
        Page<Order> page = new Page<>(current, size);
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        
        // 如果userId不为null，则按用户ID筛选
        if (userId != null) {
            wrapper.eq("user_id", userId);
        }
        
        if (status != null && !status.trim().isEmpty()) {
            wrapper.eq("status", status);
        }
        
        wrapper.orderByDesc("create_time");
        
        return orderMapper.selectPage(page, wrapper);
    }

    /**
     * 根据ID获取订单
     */
    public Order getOrderById(Long id) {
        return orderMapper.selectById(id);
    }

    /**
     * 获取订单详情
     */
    public List<OrderItem> getOrderItems(Long orderId) {
        QueryWrapper<OrderItem> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id", orderId);
        return orderItemMapper.selectList(wrapper);
    }

    /**
     * 更新订单状态
     */
    public boolean updateOrderStatus(Long id, String status) {
        Order order = new Order();
        order.setId(id);
        order.setStatus(status);
        order.setUpdateTime(LocalDateTime.now());
        return orderMapper.updateById(order) > 0;
    }

    /**
     * 支付订单
     */
    public boolean payOrder(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        
        if (!Order.Status.PENDING.getCode().equals(order.getStatus())) {
            throw new RuntimeException("订单状态不正确，无法支付");
        }
        
        return updateOrderStatus(id, Order.Status.PAID.getCode());
    }

    /**
     * 取消订单
     */
    public boolean cancelOrder(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        
        if (Order.Status.COMPLETED.getCode().equals(order.getStatus()) ||
            Order.Status.CANCELLED.getCode().equals(order.getStatus())) {
            throw new RuntimeException("订单已完成或已取消，无法取消");
        }
        
        return updateOrderStatus(id, Order.Status.CANCELLED.getCode());
    }

    /**
     * 获取用户订单统计数据
     */
    public OrderController.UserOrderStats getUserOrderStats(Long userId) {
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        
        // 获取总订单数
        Integer totalOrders = Math.toIntExact(orderMapper.selectCount(wrapper));
        
        // 获取总消费金额
        List<Order> orders = orderMapper.selectList(wrapper);
        BigDecimal totalAmount = orders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 收藏商品数（暂时返回0，后续可以扩展）
        Integer favoriteCount = 0;
        
        return new OrderController.UserOrderStats(
                totalOrders,
                totalAmount.toString(),
                favoriteCount
        );
    }

    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.valueOf((int)(Math.random() * 1000));
        return "CT" + timestamp + String.format("%03d", Integer.parseInt(random));
    }
}