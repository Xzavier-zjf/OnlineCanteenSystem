package com.canteen.order.dto;

import com.canteen.order.entity.Order;
import com.canteen.order.entity.OrderItem;
import lombok.Data;

import java.util.List;

/**
 * 订单详情响应DTO
 */
@Data
public class OrderDetailResponse {
    
    private Order order;
    
    private List<OrderItem> items;
    
    public OrderDetailResponse(Order order, List<OrderItem> items) {
        this.order = order;
        this.items = items;
    }
}