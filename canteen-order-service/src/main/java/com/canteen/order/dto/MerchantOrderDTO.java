package com.canteen.order.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 商户订单相关DTO
 */
public class MerchantOrderDTO {

    /**
     * 更新订单状态请求
     */
    @Data
    public static class UpdateStatusRequest {
        @NotNull(message = "商户ID不能为空")
        private Long merchantId;
        
        @NotNull(message = "订单状态不能为空")
        private String status;
    }
}