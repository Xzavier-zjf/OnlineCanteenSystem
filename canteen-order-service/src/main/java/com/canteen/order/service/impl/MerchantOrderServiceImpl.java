package com.canteen.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.canteen.order.entity.Order;
import com.canteen.order.entity.OrderItem;
import com.canteen.order.mapper.OrderItemMapper;
import com.canteen.order.mapper.OrderMapper;
import com.canteen.order.service.MerchantOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 商户订单服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantOrderServiceImpl implements MerchantOrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;

    @Override
    public Long getPendingOrderCount(Long merchantId) {
        try {
            // 使用新的Mapper方法根据商户ID筛选待处理订单
            return (long) orderMapper.countPendingOrdersByMerchantId(merchantId);
        } catch (Exception e) {
            log.error("获取商户待处理订单数失败：merchantId={}", merchantId, e);
            return 0L;
        }
    }

    @Override
    public BigDecimal getTodayRevenue(Long merchantId) {
        try {
            // 获取商户的所有订单，然后筛选今日的
            List<Order> merchantOrders = orderMapper.selectByMerchantId(merchantId);
            
            LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
            LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);
            
            return merchantOrders.stream()
                    .filter(order -> {
                        LocalDateTime createTime = order.getCreateTime();
                        return createTime.isAfter(startOfDay) && createTime.isBefore(endOfDay);
                    })
                    .filter(order -> {
                        String status = order.getStatus();
                        return Order.Status.PAID.getCode().equals(status) || 
                               Order.Status.PREPARING.getCode().equals(status) ||
                               Order.Status.READY.getCode().equals(status) || 
                               Order.Status.COMPLETED.getCode().equals(status);
                    })
                    .map(Order::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        } catch (Exception e) {
            log.error("获取商户今日营业额失败：merchantId={}", merchantId, e);
            return BigDecimal.ZERO;
        }
    }

    @Override
    public Long getTotalOrderCount(Long merchantId) {
        try {
            // 根据商户ID获取订单总数
            List<Order> merchantOrders = orderMapper.selectByMerchantId(merchantId);
            return (long) merchantOrders.size();
        } catch (Exception e) {
            log.error("获取商户订单总数失败：merchantId={}", merchantId, e);
            return 0L;
        }
    }

    @Override
    public List<Map<String, Object>> getOrderTrends(Long merchantId, Integer days) {
        try {
            List<Map<String, Object>> trends = new ArrayList<>();
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
                BigDecimal dayRevenue = dayOrders.stream()
                        .map(Order::getTotalAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                Map<String, Object> trend = new HashMap<>();
                trend.put("date", currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                trend.put("orderCount", (long) dayOrders.size());
                trend.put("revenue", dayRevenue);
                trends.add(trend);
            }

            return trends;
        } catch (Exception e) {
            log.error("获取商户订单趋势失败：merchantId={}", merchantId, e);
            return new ArrayList<>();
        }
    }

    @Override
    public Map<String, Object> getMerchantOrderStats(Long merchantId, String startDate, String endDate) {
        try {
            QueryWrapper<Order> wrapper = new QueryWrapper<>();
            
            // 时间范围筛选
            if (StringUtils.hasText(startDate)) {
                wrapper.ge("create_time", startDate + " 00:00:00");
            }
            if (StringUtils.hasText(endDate)) {
                wrapper.le("create_time", endDate + " 23:59:59");
            }

            // 统计各状态订单数
            Map<String, Object> stats = new HashMap<>();
            
            // 总订单数
            Long totalOrders = orderMapper.selectCount(wrapper);
            stats.put("totalOrders", totalOrders);

            // 待处理订单
            QueryWrapper<Order> pendingWrapper = (QueryWrapper<Order>) wrapper.clone();
            pendingWrapper.eq("status", Order.Status.PAID.getCode());
            Long pendingOrders = orderMapper.selectCount(pendingWrapper);
            stats.put("pendingOrders", pendingOrders);

            // 已完成订单
            QueryWrapper<Order> completedWrapper = (QueryWrapper<Order>) wrapper.clone();
            completedWrapper.eq("status", Order.Status.COMPLETED.getCode());
            Long completedOrders = orderMapper.selectCount(completedWrapper);
            stats.put("completedOrders", completedOrders);

            // 已取消订单
            QueryWrapper<Order> cancelledWrapper = (QueryWrapper<Order>) wrapper.clone();
            cancelledWrapper.eq("status", Order.Status.CANCELLED.getCode());
            Long cancelledOrders = orderMapper.selectCount(cancelledWrapper);
            stats.put("cancelledOrders", cancelledOrders);

            // 每日统计
            List<Map<String, Object>> dailyStats = getDailyStats(merchantId, startDate, endDate);
            stats.put("dailyStats", dailyStats);

            return stats;
        } catch (Exception e) {
            log.error("获取商户订单统计失败：merchantId={}", merchantId, e);
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalOrders", 0L);
            stats.put("pendingOrders", 0L);
            stats.put("completedOrders", 0L);
            stats.put("cancelledOrders", 0L);
            stats.put("dailyStats", new ArrayList<>());
            return stats;
        }
    }

    @Override
    public Map<String, Object> getMerchantFinanceStats(Long merchantId, String startDate, String endDate) {
        try {
            QueryWrapper<Order> wrapper = new QueryWrapper<>();
            wrapper.in("status", Order.Status.PAID.getCode(), Order.Status.PREPARING.getCode(), 
                      Order.Status.READY.getCode(), Order.Status.COMPLETED.getCode());
            
            // 时间范围筛选
            if (StringUtils.hasText(startDate)) {
                wrapper.ge("create_time", startDate + " 00:00:00");
            }
            if (StringUtils.hasText(endDate)) {
                wrapper.le("create_time", endDate + " 23:59:59");
            }

            List<Order> orders = orderMapper.selectList(wrapper);
            
            Map<String, Object> stats = new HashMap<>();
            
            // 总收入
            BigDecimal totalRevenue = orders.stream()
                    .map(Order::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            stats.put("totalRevenue", totalRevenue);

            // 今日收入
            BigDecimal todayRevenue = getTodayRevenue(merchantId);
            stats.put("todayRevenue", todayRevenue);

            // 平均订单金额
            BigDecimal avgOrderAmount = orders.isEmpty() ? BigDecimal.ZERO : 
                    totalRevenue.divide(new BigDecimal(orders.size()), 2, RoundingMode.HALF_UP);
            stats.put("avgOrderAmount", avgOrderAmount);

            // 月度收入
            List<Map<String, Object>> monthlyRevenues = getMonthlyRevenues(merchantId, startDate, endDate);
            stats.put("monthlyRevenues", monthlyRevenues);

            return stats;
        } catch (Exception e) {
            log.error("获取商户财务统计失败：merchantId={}", merchantId, e);
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalRevenue", BigDecimal.ZERO);
            stats.put("todayRevenue", BigDecimal.ZERO);
            stats.put("avgOrderAmount", BigDecimal.ZERO);
            stats.put("monthlyRevenues", new ArrayList<>());
            return stats;
        }
    }

    @Override
    public Map<String, Object> getMerchantOrderList(Long merchantId, Integer page, Integer size, String status) {
        try {
            Page<Order> pageParam = new Page<>(page, size);
            QueryWrapper<Order> wrapper = new QueryWrapper<>();

            // 状态筛选
            if (StringUtils.hasText(status)) {
                wrapper.eq("status", status);
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
            log.error("获取商户订单列表失败：merchantId={}", merchantId, e);
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
    @Transactional
    public void acceptOrder(Long orderId, Long merchantId) {
        try {
            Order order = orderMapper.selectById(orderId);
            if (order == null) {
                throw new RuntimeException("订单不存在");
            }

            if (!Order.Status.PAID.getCode().equals(order.getStatus())) {
                throw new RuntimeException("订单状态不正确，无法接单");
            }

            order.setStatus(Order.Status.PREPARING.getCode());
            order.setUpdateTime(LocalDateTime.now());
            orderMapper.updateById(order);

            log.info("商户接单成功：merchantId={}, orderId={}", merchantId, orderId);
        } catch (Exception e) {
            log.error("商户接单失败：merchantId={}, orderId={}", merchantId, orderId, e);
            throw new RuntimeException("接单失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void rejectOrder(Long orderId, Long merchantId, String reason) {
        try {
            Order order = orderMapper.selectById(orderId);
            if (order == null) {
                throw new RuntimeException("订单不存在");
            }

            if (!Order.Status.PAID.getCode().equals(order.getStatus())) {
                throw new RuntimeException("订单状态不正确，无法拒单");
            }

            order.setStatus(Order.Status.CANCELLED.getCode());
            order.setRemark(order.getRemark() + " [拒单原因：" + reason + "]");
            order.setUpdateTime(LocalDateTime.now());
            orderMapper.updateById(order);

            log.info("商户拒单成功：merchantId={}, orderId={}, reason={}", merchantId, orderId, reason);
        } catch (Exception e) {
            log.error("商户拒单失败：merchantId={}, orderId={}", merchantId, orderId, e);
            throw new RuntimeException("拒单失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void updateOrderStatus(Long orderId, Long merchantId, String status) {
        try {
            Order order = orderMapper.selectById(orderId);
            if (order == null) {
                throw new RuntimeException("订单不存在");
            }

            // 验证状态转换的合法性
            if (!isValidStatusTransition(order.getStatus(), status)) {
                throw new RuntimeException("订单状态转换不合法");
            }

            order.setStatus(status);
            order.setUpdateTime(LocalDateTime.now());
            orderMapper.updateById(order);

            log.info("订单状态更新成功：merchantId={}, orderId={}, status={}", merchantId, orderId, status);
        } catch (Exception e) {
            log.error("更新订单状态失败：merchantId={}, orderId={}", merchantId, orderId, e);
            throw new RuntimeException("更新订单状态失败：" + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getOrderDetail(Long orderId, Long merchantId) {
        try {
            Order order = orderMapper.selectById(orderId);
            if (order == null) {
                throw new RuntimeException("订单不存在");
            }

            Map<String, Object> orderDetail = convertOrderToMap(order);
            return orderDetail;
        } catch (Exception e) {
            log.error("获取订单详情失败：merchantId={}, orderId={}", merchantId, orderId, e);
            throw new RuntimeException("获取订单详情失败：" + e.getMessage());
        }
    }

    // 私有辅助方法
    private List<Map<String, Object>> getDailyStats(Long merchantId, String startDate, String endDate) {
        // 实现每日统计逻辑
        List<Map<String, Object>> dailyStats = new ArrayList<>();
        
        LocalDate start = StringUtils.hasText(startDate) ? 
                LocalDate.parse(startDate) : LocalDate.now().minusDays(7);
        LocalDate end = StringUtils.hasText(endDate) ? 
                LocalDate.parse(endDate) : LocalDate.now();

        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(23, 59, 59);

            QueryWrapper<Order> wrapper = new QueryWrapper<>();
            wrapper.in("status", Order.Status.PAID.getCode(), Order.Status.PREPARING.getCode(), 
                      Order.Status.READY.getCode(), Order.Status.COMPLETED.getCode());
            wrapper.between("create_time", startOfDay, endOfDay);

            List<Order> dayOrders = orderMapper.selectList(wrapper);
            BigDecimal dayRevenue = dayOrders.stream()
                    .map(Order::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Map<String, Object> dailyStat = new HashMap<>();
            dailyStat.put("date", date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            dailyStat.put("orderCount", (long) dayOrders.size());
            dailyStat.put("revenue", dayRevenue);
            dailyStats.add(dailyStat);
        }

        return dailyStats;
    }

    private List<Map<String, Object>> getMonthlyRevenues(Long merchantId, String startDate, String endDate) {
        // 实现月度收入统计逻辑
        List<Map<String, Object>> monthlyRevenues = new ArrayList<>();
        
        // 简化实现：返回最近6个月的数据
        LocalDate now = LocalDate.now();
        for (int i = 5; i >= 0; i--) {
            LocalDate monthStart = now.minusMonths(i).withDayOfMonth(1);
            LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);

            QueryWrapper<Order> wrapper = new QueryWrapper<>();
            wrapper.in("status", Order.Status.PAID.getCode(), Order.Status.PREPARING.getCode(), 
                      Order.Status.READY.getCode(), Order.Status.COMPLETED.getCode());
            wrapper.between("create_time", monthStart.atStartOfDay(), monthEnd.atTime(23, 59, 59));

            List<Order> monthOrders = orderMapper.selectList(wrapper);
            BigDecimal monthRevenue = monthOrders.stream()
                    .map(Order::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Map<String, Object> monthlyRevenue = new HashMap<>();
            monthlyRevenue.put("month", monthStart.format(DateTimeFormatter.ofPattern("yyyy-MM")));
            monthlyRevenue.put("revenue", monthRevenue);
            monthlyRevenue.put("orderCount", (long) monthOrders.size());
            monthlyRevenues.add(monthlyRevenue);
        }

        return monthlyRevenues;
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

    private boolean isValidStatusTransition(String currentStatus, String newStatus) {
        // 定义合法的状态转换
        Map<String, List<String>> validTransitions = new HashMap<>();
        validTransitions.put(Order.Status.PAID.getCode(), 
                Arrays.asList(Order.Status.PREPARING.getCode(), Order.Status.CANCELLED.getCode()));
        validTransitions.put(Order.Status.PREPARING.getCode(), 
                Arrays.asList(Order.Status.READY.getCode(), Order.Status.CANCELLED.getCode()));
        validTransitions.put(Order.Status.READY.getCode(), 
                Arrays.asList(Order.Status.COMPLETED.getCode()));

        List<String> allowedStatuses = validTransitions.get(currentStatus);
        return allowedStatuses != null && allowedStatuses.contains(newStatus);
    }
    
    @Override
    public List<Order> getPendingOrders(Long merchantId) {
        // 通过订单项中的商品关联商户ID来筛选订单
        return orderMapper.selectPendingOrdersByMerchantId(merchantId);
    }
}