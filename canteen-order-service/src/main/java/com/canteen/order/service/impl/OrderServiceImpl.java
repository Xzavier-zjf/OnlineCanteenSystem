package com.canteen.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.canteen.common.exception.BusinessException;
import com.canteen.order.controller.OrderController;
import com.canteen.order.entity.Order;
import com.canteen.order.entity.OrderItem;
import com.canteen.order.mapper.OrderItemMapper;
import com.canteen.order.mapper.OrderMapper;
import com.canteen.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

/**
 * 订单服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;

    @Override
    @Transactional
    public Order createOrder(Long userId, List<OrderItem> items, String remark) {
        try {
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

            // 保存订单
            int result = orderMapper.insert(order);
            if (result <= 0) {
                throw new BusinessException("创建订单失败");
            }

            // 保存订单项
            for (OrderItem item : items) {
                item.setOrderId(order.getId());
                item.setSubtotal(item.getPrice().multiply(new BigDecimal(item.getQuantity())));
                orderItemMapper.insert(item);
            }

            log.info("订单创建成功: {}", order.getOrderNo());
            return order;
        } catch (Exception e) {
            log.error("创建订单失败: {}", e.getMessage(), e);
            throw new BusinessException("创建订单失败: " + e.getMessage());
        }
    }

    @Override
    public Page<Order> getUserOrders(Long userId, Long current, Long size, String status) {
        try {
            Page<Order> page = new Page<>(current, size);
            QueryWrapper<Order> wrapper = new QueryWrapper<>();
            
            if (userId != null) {
                wrapper.eq("user_id", userId);
            }
            
            if (status != null && !status.trim().isEmpty()) {
                wrapper.eq("status", status);
            }
            
            wrapper.orderByDesc("create_time");
            
            return orderMapper.selectPage(page, wrapper);
        } catch (Exception e) {
            log.error("获取订单列表失败: {}", e.getMessage(), e);
            return new Page<>(current, size);
        }
    }

    @Override
    public Order getOrderById(Long id) {
        return orderMapper.selectById(id);
    }

    @Override
    public List<OrderItem> getOrderItems(Long orderId) {
        QueryWrapper<OrderItem> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id", orderId);
        return orderItemMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public boolean updateOrderStatus(Long orderId, String status) {
        try {
            Order order = orderMapper.selectById(orderId);
            if (order == null) {
                throw new BusinessException("订单不存在");
            }

            order.setStatus(status);
            order.setUpdateTime(LocalDateTime.now());

            int result = orderMapper.updateById(order);
            return result > 0;
        } catch (Exception e) {
            log.error("更新订单状态失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean payOrder(Long orderId) {
        return updateOrderStatus(orderId, Order.Status.PAID.getCode());
    }

    @Override
    @Transactional
    public boolean cancelOrder(Long orderId) {
        return updateOrderStatus(orderId, Order.Status.CANCELLED.getCode());
    }

    @Override
    public OrderController.UserOrderStats getUserOrderStats(Long userId) {
        try {
            // 查询用户总订单数
            QueryWrapper<Order> orderWrapper = new QueryWrapper<>();
            orderWrapper.eq("user_id", userId);
            Integer totalOrders = Math.toIntExact(orderMapper.selectCount(orderWrapper));

            // 查询用户总消费金额
            QueryWrapper<Order> paidOrderWrapper = new QueryWrapper<>();
            paidOrderWrapper.eq("user_id", userId);
            paidOrderWrapper.in("status", Order.Status.PAID.getCode(), Order.Status.PREPARING.getCode(), Order.Status.COMPLETED.getCode());
            
            List<Order> paidOrders = orderMapper.selectList(paidOrderWrapper);
            BigDecimal totalAmount = paidOrders.stream()
                    .map(Order::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 收藏商品数暂时返回0（需要实现收藏功能）
            Integer favoriteCount = 0;

            return new OrderController.UserOrderStats(totalOrders, totalAmount.toString(), favoriteCount);
        } catch (Exception e) {
            log.error("获取用户订单统计失败: {}", e.getMessage(), e);
            return new OrderController.UserOrderStats(0, "0.00", 0);
        }
    }

    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%04d", new Random().nextInt(10000));
        return "CT" + timestamp + random;
    }
}