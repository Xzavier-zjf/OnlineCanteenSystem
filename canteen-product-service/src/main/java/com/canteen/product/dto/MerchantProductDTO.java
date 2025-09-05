package com.canteen.product.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 商户商品相关DTO
 */
public class MerchantProductDTO {

    /**
     * 添加商品请求
     */
    @Data
    public static class AddProductRequest {
        @NotBlank(message = "商品名称不能为空")
        private String name;
        
        private String description;
        
        @NotNull(message = "价格不能为空")
        @DecimalMin(value = "0.01", message = "价格必须大于0")
        private BigDecimal price;
        
        @NotNull(message = "分类ID不能为空")
        private Long categoryId;
        
        private String imageUrl;
        
        @Min(value = 0, message = "库存不能小于0")
        private Integer stock = 0;
    }

    /**
     * 更新商品请求
     */
    @Data
    public static class UpdateProductRequest {
        private String name;
        private String description;
        
        @DecimalMin(value = "0.01", message = "价格必须大于0")
        private BigDecimal price;
        
        private Long categoryId;
        private String imageUrl;
        
        @Min(value = 0, message = "库存不能小于0")
        private Integer stock;
    }
}