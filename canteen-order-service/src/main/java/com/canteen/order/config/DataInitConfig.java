package com.canteen.order.config;

import com.canteen.order.entity.Order;
import com.canteen.order.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 数据初始化配置
 * 在应用启动时检查并初始化基础数据
 */
@Slf4j
@Component
public class DataInitConfig implements ApplicationRunner {

    @Autowired
    private OrderMapper orderMapper;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("开始检查订单数据...");
        
        // 检查是否有订单数据
        List<Order> orders = orderMapper.selectList(null);
        if (orders.isEmpty()) {
            log.warn("未发现订单数据，请确保已执行数据库初始化脚本 database/real_test_data.sql");
        } else {
            log.info("发现 {} 个订单数据", orders.size());
            
            // 统计各状态订单数量
            Map<String, Long> statusCount = orders.stream()
                .collect(Collectors.groupingBy(Order::getStatus, Collectors.counting()));
            
            log.info("订单状态统计:");
            statusCount.forEach((status, count) -> 
                log.info("  {}: {} 个", status, count)
            );
            
            // 计算总金额
            double totalAmount = orders.stream()
                .mapToDouble(order -> order.getTotalAmount().doubleValue())
                .sum();
            log.info("订单总金额: {}", totalAmount);
        }
    }
}