package com.canteen.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.canteen.order.entity.Order;
import com.canteen.order.entity.OrderItem;
import com.canteen.order.mapper.OrderItemMapper;
import com.canteen.order.mapper.OrderMapper;
import com.canteen.order.service.AdminOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 管理员订单服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminOrderServiceImpl implements AdminOrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;

    @Override
    public Long getTotalOrderCount() {
        try {
            return orderMapper.selectCount(null);
        } catch (Exception e) {
            log.error("获取订单总数失败", e);
            return 0L;
        }
    }

    @Override
    public BigDecimal getTodaySales() {
        try {
            LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
            LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);

            QueryWrapper<Order> wrapper = new QueryWrapper<>();
            wrapper.in("status", Order.Status.PAID.getCode(), Order.Status.PREPARING.getCode(), 
                      Order.Status.READY.getCode(), Order.Status.COMPLETED.getCode());
            wrapper.between("create_time", startOfDay, endOfDay);

            List<Order> todayOrders = orderMapper.selectList(wrapper);
            return todayOrders.stream()
                    .map(Order::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        } catch (Exception e) {
            log.error("获取今日销售额失败", e);
            return BigDecimal.ZERO;
        }
    }

    @Override
    public BigDecimal getTotalSales() {
        try {
            QueryWrapper<Order> wrapper = new QueryWrapper<>();
            wrapper.in("status", Order.Status.PAID.getCode(), Order.Status.PREPARING.getCode(), 
                      Order.Status.READY.getCode(), Order.Status.COMPLETED.getCode());

            List<Order> paidOrders = orderMapper.selectList(wrapper);
            return paidOrders.stream()
                    .map(Order::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        } catch (Exception e) {
            log.error("获取总销售额失败", e);
            return BigDecimal.ZERO;
        }
    }

    @Override
    public List<Map<String, Object>> getOrderStatusStats() {
        try {
            List<Map<String, Object>> stats = new ArrayList<>();
            
            // 统计各状态订单数量
            for (Order.Status status : Order.Status.values()) {
                QueryWrapper<Order> wrapper = new QueryWrapper<>();
                wrapper.eq("status", status.getCode());
                Long count = orderMapper.selectCount(wrapper);
                
                Map<String, Object> stat = new HashMap<>();
                stat.put("status", status.getDescription());
                stat.put("count", count);
                stats.add(stat);
            }
            
            return stats;
        } catch (Exception e) {
            log.error("获取订单状态统计失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<Map<String, Object>> getSalesStats(Integer days) {
        try {
            List<Map<String, Object>> stats = new ArrayList<>();
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(days - 1);

            for (int i = 0; i < days; i++) {
                LocalDate currentDate = startDate.plusDays(i);
                LocalDateTime startOfDay = currentDate.atStartOfDay();
                LocalDateTime endOfDay = currentDate.atTime(23, 59, 59);

                QueryWrapper<Order> wrapper = new QueryWrapper<>();
                wrapper.in("status", Order.Status.PAID.getCode(), Order.Status.PREPARING.getCode(), 
                          Order.Status.READY.getCode(), Order.Status.COMPLETED.getCode());
                wrapper.between("create_time", startOfDay, endOfDay);

                List<Order> dayOrders = orderMapper.selectList(wrapper);
                BigDecimal dayAmount = dayOrders.stream()
                        .map(Order::getTotalAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                Map<String, Object> stat = new HashMap<>();
                stat.put("date", currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                stat.put("amount", dayAmount);
                stat.put("orderCount", (long) dayOrders.size());
                stats.add(stat);
            }

            return stats;
        } catch (Exception e) {
            log.error("获取销售统计失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public Map<String, Object> getUserOrderStats(Long userId) {
        try {
            Map<String, Object> stats = new HashMap<>();

            // 查询用户总订单数
            QueryWrapper<Order> orderWrapper = new QueryWrapper<>();
            orderWrapper.eq("user_id", userId);
            Long totalOrders = orderMapper.selectCount(orderWrapper);

            // 查询用户总消费金额
            QueryWrapper<Order> paidOrderWrapper = new QueryWrapper<>();
            paidOrderWrapper.eq("user_id", userId);
            paidOrderWrapper.in("status", Order.Status.PAID.getCode(), Order.Status.PREPARING.getCode(), 
                               Order.Status.READY.getCode(), Order.Status.COMPLETED.getCode());
            
            List<Order> paidOrders = orderMapper.selectList(paidOrderWrapper);
            BigDecimal totalSpent = paidOrders.stream()
                    .map(Order::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            stats.put("totalOrders", totalOrders);
            stats.put("totalSpent", totalSpent);

            return stats;
        } catch (Exception e) {
            log.error("获取用户订单统计失败：userId={}", userId, e);
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalOrders", 0L);
            stats.put("totalSpent", BigDecimal.ZERO);
            return stats;
        }
    }

    @Override
    public Map<String, Object> getAdminOrderList(Integer page, Integer size, String status, String startDate, String endDate) {
        try {
            Page<Order> pageParam = new Page<>(page, size);
            QueryWrapper<Order> wrapper = new QueryWrapper<>();

            // 状态筛选
            if (StringUtils.hasText(status)) {
                wrapper.eq("status", status);
            }

            // 时间范围筛选
            if (StringUtils.hasText(startDate)) {
                wrapper.ge("create_time", startDate + " 00:00:00");
            }
            if (StringUtils.hasText(endDate)) {
                wrapper.le("create_time", endDate + " 23:59:59");
            }

            wrapper.orderByDesc("create_time");

            Page<Order> orderPage = orderMapper.selectPage(pageParam, wrapper);

            // 构建返回结果
            List<Map<String, Object>> orderList = orderPage.getRecords().stream()
                    .map(this::convertOrderToMap)
                    .collect(Collectors.toList());

            Map<String, Object> result = new HashMap<>();
            result.put("records", orderList);
            result.put("total", orderPage.getTotal());
            result.put("page", page);
            result.put("size", size);
            result.put("pages", orderPage.getPages());

            return result;
        } catch (Exception e) {
            log.error("获取管理员订单列表失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("records", new ArrayList<>());
            result.put("total", 0L);
            result.put("page", page);
            result.put("size", size);
            result.put("pages", 0L);
            return result;
        }
    }

    private Map<String, Object> convertOrderToMap(Order order) {
        Map<String, Object> orderMap = new HashMap<>();
        orderMap.put("id", order.getId());
        orderMap.put("orderNo", order.getOrderNo());
        orderMap.put("userId", order.getUserId());
        orderMap.put("totalAmount", order.getTotalAmount());
        orderMap.put("status", order.getStatus());
        orderMap.put("statusDesc", getStatusDescription(order.getStatus()));
        orderMap.put("remark", order.getRemark());
        orderMap.put("createTime", order.getCreateTime());
        orderMap.put("updateTime", order.getUpdateTime());

        // 获取订单项
        try {
            QueryWrapper<OrderItem> itemWrapper = new QueryWrapper<>();
            itemWrapper.eq("order_id", order.getId());
            List<OrderItem> items = orderItemMapper.selectList(itemWrapper);
            orderMap.put("items", items);
        } catch (Exception e) {
            log.warn("获取订单项失败：orderId={}", order.getId());
            orderMap.put("items", new ArrayList<>());
        }

        return orderMap;
    }

    private String getStatusDescription(String status) {
        for (Order.Status orderStatus : Order.Status.values()) {
            if (orderStatus.getCode().equals(status)) {
                return orderStatus.getDescription();
            }
        }
        return status;
    }

    @Override
    public Map<String, Object> getAllOrders(Integer page, Integer size, String status) {
        try {
            int offset = (page - 1) * size;
            
            // 获取订单列表
            List<Order> orders;
            long total;
            
            if (StringUtils.hasText(status)) {
                QueryWrapper<Order> wrapper = new QueryWrapper<>();
                wrapper.eq("status", status);
                wrapper.orderByDesc("create_time");
                
                Page<Order> pageParam = new Page<>(page, size);
                Page<Order> orderPage = orderMapper.selectPage(pageParam, wrapper);
                orders = orderPage.getRecords();
                total = orderPage.getTotal();
            } else {
                orders = orderMapper.selectAllOrdersWithPagination(offset, size);
                total = orderMapper.countAllOrders();
            }
            
            // 转换为返回格式
            List<Map<String, Object>> orderList = orders.stream()
                    .map(this::convertOrderToMap)
                    .collect(Collectors.toList());
            
            Map<String, Object> result = new HashMap<>();
            result.put("records", orderList);
            result.put("total", total);
            result.put("page", page);
            result.put("size", size);
            result.put("pages", (total + size - 1) / size);
            
            return result;
        } catch (Exception e) {
            log.error("获取所有订单失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("records", new ArrayList<>());
            result.put("total", 0L);
            result.put("page", page);
            result.put("size", size);
            result.put("pages", 0L);
            return result;
        }
    }

    @Override
    public Map<String, Object> getOrderStatistics() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // 总订单数
            int totalOrders = orderMapper.countAllOrders();
            stats.put("totalOrders", totalOrders);
            
            // 各状态订单数
            stats.put("pendingOrders", orderMapper.countOrdersByStatus(Order.Status.PENDING.getCode()));
            stats.put("paidOrders", orderMapper.countOrdersByStatus(Order.Status.PAID.getCode()));
            stats.put("preparingOrders", orderMapper.countOrdersByStatus(Order.Status.PREPARING.getCode()));
            stats.put("readyOrders", orderMapper.countOrdersByStatus(Order.Status.READY.getCode()));
            stats.put("completedOrders", orderMapper.countOrdersByStatus(Order.Status.COMPLETED.getCode()));
            stats.put("cancelledOrders", orderMapper.countOrdersByStatus(Order.Status.CANCELLED.getCode()));
            
            // 今日订单数
            LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
            LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);
            
            QueryWrapper<Order> todayWrapper = new QueryWrapper<>();
            todayWrapper.between("create_time", startOfDay, endOfDay);
            int todayOrders = Math.toIntExact(orderMapper.selectCount(todayWrapper));
            stats.put("todayOrders", todayOrders);
            
            return stats;
        } catch (Exception e) {
            log.error("获取订单统计失败", e);
            return new HashMap<>();
        }
    }

    @Override
    public Map<String, Object> getSalesStatistics(String startDate, String endDate) {
        try {
            // 设置默认时间范围（最近30天）
            if (!StringUtils.hasText(startDate)) {
                startDate = LocalDate.now().minusDays(30).toString();
            }
            if (!StringUtils.hasText(endDate)) {
                endDate = LocalDate.now().toString();
            }
            
            // 获取时间范围内的统计数据
            List<Map<String, Object>> dailyStats = orderMapper.getOrderStatsByDateRange(
                    startDate + " 00:00:00", endDate + " 23:59:59");
            
            // 计算总计
            BigDecimal totalAmount = dailyStats.stream()
                    .map(stat -> (BigDecimal) stat.get("totalAmount"))
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            Long totalOrderCount = dailyStats.stream()
                    .map(stat -> ((Number) stat.get("orderCount")).longValue())
                    .reduce(0L, Long::sum);
            
            Map<String, Object> result = new HashMap<>();
            result.put("dailyStats", dailyStats);
            result.put("totalAmount", totalAmount);
            result.put("totalOrderCount", totalOrderCount);
            result.put("startDate", startDate);
            result.put("endDate", endDate);
            
            return result;
        } catch (Exception e) {
            log.error("获取销售统计失败", e);
            return new HashMap<>();
        }
    }
}