package com.canteen.order.controller;

import com.canteen.common.result.Result;
import com.canteen.order.dto.MerchantOrderDTO;
import com.canteen.order.service.MerchantOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

/**
 * 商户订单控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/orders/merchant")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class MerchantOrderController {

    private final MerchantOrderService merchantOrderService;

    /**
     * 获取商户待处理订单数
     */
    @GetMapping("/{merchantId}/pending/count")
    public Long getPendingOrderCount(@PathVariable Long merchantId) {
        return merchantOrderService.getPendingOrderCount(merchantId);
    }

    /**
     * 获取商户今日营业额
     */
    @GetMapping("/{merchantId}/revenue/today")
    public java.math.BigDecimal getTodayRevenue(@PathVariable Long merchantId) {
        return merchantOrderService.getTodayRevenue(merchantId);
    }

    /**
     * 获取商户订单总数
     */
    @GetMapping("/{merchantId}/count")
    public Long getTotalOrderCount(@PathVariable Long merchantId) {
        return merchantOrderService.getTotalOrderCount(merchantId);
    }

    /**
     * 获取商户订单趋势
     */
    @GetMapping("/{merchantId}/trends")
    public java.util.List<Map<String, Object>> getOrderTrends(@PathVariable Long merchantId,
                                                             @RequestParam(defaultValue = "7") Integer days) {
        return merchantOrderService.getOrderTrends(merchantId, days);
    }

    /**
     * 获取商户订单统计
     */
    @GetMapping("/{merchantId}/stats")
    public Map<String, Object> getMerchantOrderStats(@PathVariable Long merchantId,
                                                     @RequestParam(required = false) String startDate,
                                                     @RequestParam(required = false) String endDate) {
        return merchantOrderService.getMerchantOrderStats(merchantId, startDate, endDate);
    }

    /**
     * 获取商户财务统计
     */
    @GetMapping("/{merchantId}/finance")
    public Map<String, Object> getMerchantFinanceStats(@PathVariable Long merchantId,
                                                       @RequestParam(required = false) String startDate,
                                                       @RequestParam(required = false) String endDate) {
        return merchantOrderService.getMerchantFinanceStats(merchantId, startDate, endDate);
    }

    /**
     * 获取商户订单列表
     */
    @GetMapping("/{merchantId}/list")
    public Result<Map<String, Object>> getMerchantOrderList(@PathVariable Long merchantId,
                                                           @RequestParam(defaultValue = "1") Integer page,
                                                           @RequestParam(defaultValue = "10") Integer size,
                                                           @RequestParam(required = false) String status) {
        try {
            Map<String, Object> result = merchantOrderService.getMerchantOrderList(merchantId, page, size, status);
            return Result.success("获取订单列表成功", result);
        } catch (Exception e) {
            log.error("获取商户订单列表失败", e);
            return Result.error("获取订单列表失败");
        }
    }

    /**
     * 商户接单
     */
    @PutMapping("/{orderId}/accept")
    public Result<String> acceptOrder(@PathVariable Long orderId, @RequestParam Long merchantId) {
        try {
            merchantOrderService.acceptOrder(orderId, merchantId);
            return Result.success("接单成功");
        } catch (Exception e) {
            log.error("接单失败", e);
            return Result.error("接单失败：" + e.getMessage());
        }
    }

    /**
     * 商户拒单
     */
    @PutMapping("/{orderId}/reject")
    public Result<String> rejectOrder(@PathVariable Long orderId, 
                                    @RequestParam Long merchantId,
                                    @RequestParam(required = false) String reason) {
        try {
            merchantOrderService.rejectOrder(orderId, merchantId, reason);
            return Result.success("拒单成功");
        } catch (Exception e) {
            log.error("拒单失败", e);
            return Result.error("拒单失败：" + e.getMessage());
        }
    }

    /**
     * 更新订单状态
     */
    @PutMapping("/{orderId}/status")
    public Result<String> updateOrderStatus(@PathVariable Long orderId,
                                           @Valid @RequestBody MerchantOrderDTO.UpdateStatusRequest request) {
        try {
            merchantOrderService.updateOrderStatus(orderId, request.getMerchantId(), request.getStatus());
            return Result.success("订单状态更新成功");
        } catch (Exception e) {
            log.error("更新订单状态失败", e);
            return Result.error("更新订单状态失败：" + e.getMessage());
        }
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/{orderId}/detail")
    public Result<Map<String, Object>> getOrderDetail(@PathVariable Long orderId, @RequestParam Long merchantId) {
        try {
            Map<String, Object> orderDetail = merchantOrderService.getOrderDetail(orderId, merchantId);
            return Result.success("获取订单详情成功", orderDetail);
        } catch (Exception e) {
            log.error("获取订单详情失败", e);
            return Result.error("获取订单详情失败");
        }
    }
}