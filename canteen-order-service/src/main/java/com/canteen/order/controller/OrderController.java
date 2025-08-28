package com.canteen.order.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.canteen.common.result.Result;
import com.canteen.common.utils.JwtUtils;
import com.canteen.order.dto.OrderDetailResponse;
import com.canteen.order.entity.Order;
import com.canteen.order.entity.OrderItem;
import com.canteen.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 订单控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 创建订单
     */
    @PostMapping
    public Result<Order> createOrder(HttpServletRequest request, @RequestBody CreateOrderRequest orderRequest) {
        try {
            Long userId = getUserIdFromToken(request);
            
            // 构建订单项列表
            List<OrderItem> items = new ArrayList<>();
            for (OrderItemRequest itemRequest : orderRequest.getItems()) {
                OrderItem item = new OrderItem();
                item.setProductId(itemRequest.getProductId());
                item.setProductName(itemRequest.getProductName());
                item.setQuantity(itemRequest.getQuantity());
                item.setPrice(itemRequest.getPrice());
                items.add(item);
            }
            
            Order order = orderService.createOrder(userId, items, orderRequest.getRemark());
            log.info("创建订单成功: {}", order.getOrderNo());
            return Result.success("订单创建成功", order);
        } catch (Exception e) {
            log.error("创建订单失败: {}", e.getMessage(), e);
            return Result.error("创建订单失败: " + e.getMessage());
        }
    }

    /**
     * 获取订单列表 - 兼容前端调用
     */
    @GetMapping("/list")
    public Result<List<Order>> getOrderListForUser(@RequestParam Long userId) {
        try {
            // 为了兼容前端，直接返回列表而非分页结果
            Page<Order> orderPage = orderService.getUserOrders(userId, 1L, 100L, null);
            return Result.success(orderPage.getRecords());
        } catch (Exception e) {
            log.error("获取订单列表失败: {}", e.getMessage(), e);
            return Result.error("获取订单列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取订单列表（分页）
     */
    @GetMapping("/page")
    public Result<Page<Order>> getOrderList(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String status) {
        
        try {
            Long userId = getUserIdFromToken(request);
            Page<Order> orderPage = orderService.getUserOrders(userId, current, size, status);
            return Result.success(orderPage);
        } catch (Exception e) {
            log.error("获取订单列表失败: {}", e.getMessage(), e);
            return Result.error("获取订单列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户订单列表
     */
    @GetMapping("/user/{userId}")
    public Result<List<Order>> getUserOrders(@PathVariable Long userId) {
        try {
            Page<Order> orderPage = orderService.getUserOrders(userId, 1L, 100L, null);
            return Result.success(orderPage.getRecords());
        } catch (Exception e) {
            log.error("获取用户订单失败: {}", e.getMessage(), e);
            return Result.error("获取用户订单失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有订单（管理员用）
     */
    @GetMapping
    public Result<List<Order>> getAllOrders() {
        try {
            Page<Order> orderPage = orderService.getUserOrders(null, 1L, 100L, null);
            return Result.success(orderPage.getRecords());
        } catch (Exception e) {
            log.error("获取所有订单失败: {}", e.getMessage(), e);
            return Result.error("获取所有订单失败: " + e.getMessage());
        }
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/{id}")
    public Result<OrderDetailResponse> getOrder(@PathVariable Long id) {
        try {
            Order order = orderService.getOrderById(id);
            if (order == null) {
                return Result.error("订单不存在");
            }
            
            List<OrderItem> items = orderService.getOrderItems(id);
            OrderDetailResponse response = new OrderDetailResponse(order, items);
            return Result.success(response);
        } catch (Exception e) {
            log.error("获取订单详情失败: {}", e.getMessage(), e);
            return Result.error("获取订单详情失败: " + e.getMessage());
        }
    }

    /**
     * 获取订单详情（带详情路径）
     */
    @GetMapping("/{id}/detail")
    public Result<OrderDetailResponse> getOrderDetail(@PathVariable Long id) {
        return getOrder(id);
    }

    /**
     * 更新订单状态
     */
    @PutMapping("/{id}/status")
    public Result<Boolean> updateOrderStatus(@PathVariable Long id, @RequestBody UpdateStatusRequest request) {
        try {
            boolean success = orderService.updateOrderStatus(id, request.getStatus());
            if (success) {
                log.info("更新订单状态成功: {} -> {}", id, request.getStatus());
                return Result.success("订单状态更新成功", true);
            } else {
                return Result.error("订单状态更新失败");
            }
        } catch (Exception e) {
            log.error("更新订单状态失败: {}", e.getMessage(), e);
            return Result.error("更新订单状态失败: " + e.getMessage());
        }
    }

    /**
     * 支付订单
     */
    @PostMapping("/{id}/pay")
    public Result<Boolean> payOrder(@PathVariable Long id) {
        try {
            boolean success = orderService.payOrder(id);
            if (success) {
                log.info("订单支付成功: {}", id);
                return Result.success("支付成功", true);
            } else {
                return Result.error("支付失败");
            }
        } catch (Exception e) {
            log.error("订单支付失败: {}", e.getMessage(), e);
            return Result.error("支付失败: " + e.getMessage());
        }
    }

    /**
     * 取消订单
     */
    @PostMapping("/{id}/cancel")
    public Result<Boolean> cancelOrder(@PathVariable Long id) {
        try {
            boolean success = orderService.cancelOrder(id);
            if (success) {
                log.info("订单取消成功: {}", id);
                return Result.success("订单已取消", true);
            } else {
                return Result.error("取消订单失败");
            }
        } catch (Exception e) {
            log.error("取消订单失败: {}", e.getMessage(), e);
            return Result.error("取消订单失败: " + e.getMessage());
        }
    }

    /**
     * 商家接单（开始制作）
     */
    @PostMapping("/{id}/prepare")
    public Result<Boolean> prepareOrder(@PathVariable Long id) {
        try {
            boolean success = orderService.updateOrderStatus(id, Order.Status.PREPARING.getCode());
            if (success) {
                log.info("订单开始制作: {}", id);
                return Result.success("订单已开始制作", true);
            } else {
                return Result.error("更新订单状态失败");
            }
        } catch (Exception e) {
            log.error("更新订单制作状态失败: {}", e.getMessage(), e);
            return Result.error("更新订单状态失败: " + e.getMessage());
        }
    }

    /**
     * 订单制作完成（待取餐）
     */
    @PostMapping("/{id}/ready")
    public Result<Boolean> readyOrder(@PathVariable Long id) {
        try {
            boolean success = orderService.updateOrderStatus(id, Order.Status.READY.getCode());
            if (success) {
                log.info("订单制作完成: {}", id);
                return Result.success("订单已完成，请及时取餐", true);
            } else {
                return Result.error("更新订单状态失败");
            }
        } catch (Exception e) {
            log.error("更新订单状态失败: {}", e.getMessage(), e);
            return Result.error("更新订单状态失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户订单统计数据
     */
    @GetMapping("/stats/user/{userId}")
    public Result<UserOrderStats> getUserOrderStats(@PathVariable Long userId) {
        try {
            UserOrderStats stats = orderService.getUserOrderStats(userId);
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取用户订单统计失败: {}", e.getMessage(), e);
            return Result.error("获取用户订单统计失败: " + e.getMessage());
        }
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("订单服务运行正常");
    }

    // 内部类：创建订单请求
    public static class CreateOrderRequest {
        private Long userId;
        private BigDecimal totalAmount;
        private String remark;
        private List<OrderItemRequest> items;

        // getter和setter
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
        public String getRemark() { return remark; }
        public void setRemark(String remark) { this.remark = remark; }
        public List<OrderItemRequest> getItems() { return items; }
        public void setItems(List<OrderItemRequest> items) { this.items = items; }
    }

    // 内部类：订单商品请求
    public static class OrderItemRequest {
        private Long productId;
        private String productName;
        private Integer quantity;
        private BigDecimal price;

        // getter和setter
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
    }

    // 内部类：更新状态请求
    public static class UpdateStatusRequest {
        private String status;

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    // 内部类：用户订单统计数据
    public static class UserOrderStats {
        private Integer totalOrders;      // 总订单数
        private String totalAmount;       // 总消费金额
        private Integer favoriteCount;    // 收藏商品数（暂时返回0）

        public UserOrderStats() {}

        public UserOrderStats(Integer totalOrders, String totalAmount, Integer favoriteCount) {
            this.totalOrders = totalOrders;
            this.totalAmount = totalAmount;
            this.favoriteCount = favoriteCount;
        }

        // getter和setter
        public Integer getTotalOrders() { return totalOrders; }
        public void setTotalOrders(Integer totalOrders) { this.totalOrders = totalOrders; }
        public String getTotalAmount() { return totalAmount; }
        public void setTotalAmount(String totalAmount) { this.totalAmount = totalAmount; }
        public Integer getFavoriteCount() { return favoriteCount; }
        public void setFavoriteCount(Integer favoriteCount) { this.favoriteCount = favoriteCount; }
    }

    /**
     * 从请求头中获取用户ID
     */
    private Long getUserIdFromToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        log.info("收到Authorization头: {}", token);
        
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            log.info("提取的Token: {}", token);
            
            try {
                Long userId = JwtUtils.getUserIdFromToken(token);
                log.info("解析出的用户ID: {}", userId);
                return userId;
            } catch (Exception e) {
                log.error("Token解析失败: {}", e.getMessage(), e);
                throw new RuntimeException("Token解析失败: " + e.getMessage());
            }
        }
        log.error("Token格式无效或为空");
        throw new RuntimeException("Token无效");
    }
}
