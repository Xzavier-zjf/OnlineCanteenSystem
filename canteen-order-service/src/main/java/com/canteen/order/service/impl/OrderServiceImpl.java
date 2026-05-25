package com.canteen.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
            // 计算总金额
            BigDecimal totalAmount = items.stream()
                    .map(item -> item.getPrice().multiply(new BigDecimal(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 创建订单
            Order order = new Order();
            order.setUserId(userId);
            order.setOrderNo(generateOrderNo());
            order.setTotalAmount(totalAmount);
            order.setStatus(Order.Status.PENDING.getCode());
            order.setRemark(remark);
            order.setCreateTime(LocalDateTime.now());
            order.setUpdateTime(LocalDateTime.now());

            orderMapper.insert(order);

            // 创建订单项
            for (OrderItem item : items) {
                item.setOrderId(order.getId());
                orderItemMapper.insert(item);
            }

            return order;
        } catch (Exception e) {
            log.error("创建订单失败", e);
            throw new RuntimeException("创建订单失败: " + e.getMessage());
        }
    }

    @Override
    public Page<Order> getUserOrders(Long userId, Long current, Long size, String status) {
        try {
            Page<Order> page = new Page<>(current, size);
            LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
            
            if (userId != null) {
                wrapper.eq(Order::getUserId, userId);
            }
            if (StringUtils.hasText(status)) {
                wrapper.eq(Order::getStatus, status);
            }
            
            wrapper.orderByDesc(Order::getCreateTime);
            return orderMapper.selectPage(page, wrapper);
        } catch (Exception e) {
            log.error("获取用户订单失败", e);
            throw new RuntimeException("获取用户订单失败: " + e.getMessage());
        }
    }

    @Override
    public Order getOrderById(Long id) {
        try {
            return orderMapper.selectById(id);
        } catch (Exception e) {
            log.error("获取订单详情失败", e);
            throw new RuntimeException("获取订单详情失败: " + e.getMessage());
        }
    }

    @Override
    public List<OrderItem> getOrderItems(Long orderId) {
        try {
            LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(OrderItem::getOrderId, orderId);
            return orderItemMapper.selectList(wrapper);
        } catch (Exception e) {
            log.error("获取订单项失败", e);
            throw new RuntimeException("获取订单项失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean updateOrderStatus(Long orderId, String status) {
        try {
            Order order = orderMapper.selectById(orderId);
            if (order == null) {
                return false;
            }
            
            order.setStatus(status);
            order.setUpdateTime(LocalDateTime.now());
            
            return orderMapper.updateById(order) > 0;
        } catch (Exception e) {
            log.error("更新订单状态失败", e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean payOrder(Long orderId) {
        try {
            return updateOrderStatus(orderId, Order.Status.PAID.getCode());
        } catch (Exception e) {
            log.error("支付订单失败", e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean cancelOrder(Long orderId) {
        try {
            return updateOrderStatus(orderId, Order.Status.CANCELLED.getCode());
        } catch (Exception e) {
            log.error("取消订单失败", e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean refundOrder(Long orderId, String reason) {
        try {
            Order order = orderMapper.selectById(orderId);
            if (order == null) {
                return false;
            }
            if (!canRefund(order.getStatus())) {
                throw new RuntimeException("当前订单状态不支持退款");
            }

            order.setStatus(Order.Status.CANCELLED.getCode());
            order.setRemark(appendReason(order.getRemark(), "退款原因", reason));
            order.setUpdateTime(LocalDateTime.now());
            return orderMapper.updateById(order) > 0;
        } catch (Exception e) {
            log.error("退款订单失败", e);
            throw new RuntimeException("退款订单失败: " + e.getMessage());
        }
    }

    @Override
    public OrderController.UserOrderStats getUserOrderStats(Long userId) {
        try {
            LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Order::getUserId, userId);
            
            // 获取总订单数
            Long totalOrders = orderMapper.selectCount(wrapper);
            
            // 获取总消费金额
            wrapper.eq(Order::getStatus, Order.Status.COMPLETED.getCode());
            List<Order> completedOrders = orderMapper.selectList(wrapper);
            BigDecimal totalAmount = completedOrders.stream()
                    .map(Order::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            return new OrderController.UserOrderStats(
                    totalOrders.intValue(),
                    totalAmount.toString(),
                    0 // 收藏数暂时返回0
            );
        } catch (Exception e) {
            log.error("获取用户订单统计失败", e);
            return new OrderController.UserOrderStats(0, "0", 0);
        }
    }

    @Override
    public long getOrderCountByStatus(String status) {
        try {
            LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
            if (StringUtils.hasText(status)) {
                wrapper.eq(Order::getStatus, status);
            }
            return orderMapper.selectCount(wrapper);
        } catch (Exception e) {
            log.error("获取订单数量失败", e);
            return 0L;
        }
    }

    @Override
    public BigDecimal getTodayRevenue() {
        try {
            LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Order::getStatus, Order.Status.COMPLETED.getCode());
            wrapper.ge(Order::getCreateTime, LocalDate.now().atStartOfDay());
            wrapper.lt(Order::getCreateTime, LocalDate.now().plusDays(1).atStartOfDay());
            
            List<Order> todayOrders = orderMapper.selectList(wrapper);
            return todayOrders.stream()
                    .map(Order::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        } catch (Exception e) {
            log.error("获取今日营业额失败", e);
            return BigDecimal.ZERO;
        }
    }

    @Override
    public long getTotalOrderCount() {
        try {
            return orderMapper.selectCount(null);
        } catch (Exception e) {
            log.error("获取总订单数失败", e);
            return 0L;
        }
    }

    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        return "ORDER" + System.currentTimeMillis();
    }

    private boolean canRefund(String status) {
        return Order.Status.PAID.getCode().equals(status)
                || Order.Status.PREPARING.getCode().equals(status)
                || Order.Status.READY.getCode().equals(status)
                || Order.Status.COMPLETED.getCode().equals(status);
    }

    private String appendReason(String remark, String label, String reason) {
        String baseRemark = StringUtils.hasText(remark) ? remark : "";
        String safeReason = StringUtils.hasText(reason) ? reason.trim() : "未填写";
        return baseRemark + " [" + label + "：" + safeReason + "]";
    }
}
