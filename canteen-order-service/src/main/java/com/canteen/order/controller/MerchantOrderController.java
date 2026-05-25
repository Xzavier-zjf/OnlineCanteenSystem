package com.canteen.order.controller;

import com.canteen.common.result.Result;
import com.canteen.common.utils.AuthContext;
import com.canteen.order.dto.MerchantOrderDTO;
import com.canteen.order.service.MerchantOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
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
    public Long getPendingOrderCount(HttpServletRequest request, @PathVariable Long merchantId) {
        merchantId = requireCurrentMerchant(request, merchantId);
        return merchantOrderService.getPendingOrderCount(merchantId);
    }

    /**
     * 获取商户今日营业额
     */
    @GetMapping("/{merchantId}/revenue/today")
    public java.math.BigDecimal getTodayRevenue(HttpServletRequest request, @PathVariable Long merchantId) {
        merchantId = requireCurrentMerchant(request, merchantId);
        return merchantOrderService.getTodayRevenue(merchantId);
    }

    /**
     * 获取商户订单总数
     */
    @GetMapping("/{merchantId}/count")
    public Long getTotalOrderCount(HttpServletRequest request, @PathVariable Long merchantId) {
        merchantId = requireCurrentMerchant(request, merchantId);
        return merchantOrderService.getTotalOrderCount(merchantId);
    }

    /**
     * 获取商户订单趋势
     */
    @GetMapping("/{merchantId}/trends")
    public java.util.List<Map<String, Object>> getOrderTrends(HttpServletRequest request,
                                                              @PathVariable Long merchantId,
                                                              @RequestParam(defaultValue = "7") Integer days) {
        merchantId = requireCurrentMerchant(request, merchantId);
        return merchantOrderService.getOrderTrends(merchantId, days);
    }

    /**
     * 获取商户订单统计
     */
    @GetMapping("/{merchantId}/stats")
    public Map<String, Object> getMerchantOrderStats(HttpServletRequest request,
                                                     @PathVariable Long merchantId,
                                                     @RequestParam(required = false) String startDate,
                                                     @RequestParam(required = false) String endDate) {
        merchantId = requireCurrentMerchant(request, merchantId);
        return merchantOrderService.getMerchantOrderStats(merchantId, startDate, endDate);
    }

    /**
     * 获取商户财务统计
     */
    @GetMapping("/{merchantId}/finance")
    public Map<String, Object> getMerchantFinanceStats(HttpServletRequest request,
                                                       @PathVariable Long merchantId,
                                                       @RequestParam(required = false) String startDate,
                                                       @RequestParam(required = false) String endDate) {
        merchantId = requireCurrentMerchant(request, merchantId);
        return merchantOrderService.getMerchantFinanceStats(merchantId, startDate, endDate);
    }

    /**
     * 获取商户订单列表
     */
    @GetMapping("/{merchantId}/list")
    public Result<Map<String, Object>> getMerchantOrderList(HttpServletRequest request,
                                                            @PathVariable Long merchantId,
                                                            @RequestParam(defaultValue = "1") Integer page,
                                                            @RequestParam(defaultValue = "10") Integer size,
                                                            @RequestParam(required = false) String status) {
        try {
            merchantId = requireCurrentMerchant(request, merchantId);
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
    public Result<String> acceptOrder(HttpServletRequest request,
                                      @PathVariable Long orderId,
                                      @RequestParam Long merchantId) {
        try {
            merchantId = requireCurrentMerchant(request, merchantId);
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
    public Result<String> rejectOrder(HttpServletRequest request,
                                    @PathVariable Long orderId,
                                    @RequestParam Long merchantId,
                                    @RequestParam(required = false) String reason) {
        try {
            merchantId = requireCurrentMerchant(request, merchantId);
            merchantOrderService.rejectOrder(orderId, merchantId, reason);
            return Result.success("拒单成功");
        } catch (Exception e) {
            log.error("拒单失败", e);
            return Result.error("拒单失败：" + e.getMessage());
        }
    }

    /**
     * 商户退款
     */
    @PutMapping("/{orderId}/refund")
    public Result<String> refundOrder(HttpServletRequest request,
                                      @PathVariable Long orderId,
                                      @Valid @RequestBody MerchantOrderDTO.RefundRequest refundRequest) {
        try {
            Long merchantId = requireCurrentMerchant(request, refundRequest.getMerchantId());
            merchantOrderService.refundOrder(orderId, merchantId, refundRequest.getReason());
            return Result.success("退款成功");
        } catch (Exception e) {
            log.error("退款失败", e);
            return Result.error("退款失败：" + e.getMessage());
        }
    }

    /**
     * 更新订单状态
     */
    @PutMapping("/{orderId}/status")
    public Result<String> updateOrderStatus(HttpServletRequest httpRequest,
                                           @PathVariable Long orderId,
                                           @Valid @RequestBody MerchantOrderDTO.UpdateStatusRequest updateRequest) {
        try {
            Long merchantId = requireCurrentMerchant(httpRequest, updateRequest.getMerchantId());
            merchantOrderService.updateOrderStatus(orderId, merchantId, updateRequest.getStatus());
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
    public Result<Map<String, Object>> getOrderDetail(HttpServletRequest request,
                                                      @PathVariable Long orderId,
                                                      @RequestParam Long merchantId) {
        try {
            merchantId = requireCurrentMerchant(request, merchantId);
            Map<String, Object> orderDetail = merchantOrderService.getOrderDetail(orderId, merchantId);
            return Result.success("获取订单详情成功", orderDetail);
        } catch (Exception e) {
            log.error("获取订单详情失败", e);
            return Result.error("获取订单详情失败");
        }
    }

    private Long requireCurrentMerchant(HttpServletRequest request, Long merchantId) {
        AuthContext auth = AuthContext.from(request);
        auth.requireRole("MERCHANT");
        if (!auth.getUserId().equals(merchantId)) {
            throw new SecurityException("无权限访问其他商户数据");
        }
        return auth.getUserId();
    }
}
