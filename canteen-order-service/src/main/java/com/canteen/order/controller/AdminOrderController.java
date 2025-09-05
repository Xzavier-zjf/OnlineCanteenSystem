package com.canteen.order.controller;

import com.canteen.common.result.Result;
import com.canteen.order.service.AdminOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 管理员订单控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    /**
     * 获取订单总数
     */
    @GetMapping("/count")
    public Long getOrderCount() {
        return adminOrderService.getTotalOrderCount();
    }

    /**
     * 获取今日销售额
     */
    @GetMapping("/sales/today")
    public BigDecimal getTodaySales() {
        return adminOrderService.getTodaySales();
    }

    /**
     * 获取总销售额
     */
    @GetMapping("/sales/total")
    public BigDecimal getTotalSales() {
        return adminOrderService.getTotalSales();
    }

    /**
     * 获取订单状态统计
     */
    @GetMapping("/stats/status")
    public List<Map<String, Object>> getOrderStatusStats() {
        return adminOrderService.getOrderStatusStats();
    }

    /**
     * 获取销售统计
     */
    @GetMapping("/stats/sales")
    public List<Map<String, Object>> getSalesStats(@RequestParam(defaultValue = "7") Integer days) {
        return adminOrderService.getSalesStats(days);
    }

    /**
     * 获取用户订单统计
     */
    @GetMapping("/user/{userId}/stats")
    public Map<String, Object> getUserOrderStats(@PathVariable Long userId) {
        return adminOrderService.getUserOrderStats(userId);
    }

    /**
     * 获取全平台订单列表（管理员）
     */
    @GetMapping("/admin/list")
    public Result<Map<String, Object>> getAdminOrderList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            Map<String, Object> result = adminOrderService.getAdminOrderList(page, size, status, startDate, endDate);
            return Result.success("获取订单列表成功", result);
        } catch (Exception e) {
            log.error("获取管理员订单列表失败", e);
            return Result.error("获取订单列表失败");
        }
    }
}