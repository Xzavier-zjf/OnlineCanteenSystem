package com.canteen.user.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class UserStatsDTO {
    private Long totalOrders;
    private BigDecimal totalSpent;
    private Long favoriteProducts;
    private Integer memberDays;
    
    public UserStatsDTO() {
        this.totalOrders = 0L;
        this.totalSpent = BigDecimal.ZERO;
        this.favoriteProducts = 0L;
        this.memberDays = 0;
    }
    
    public UserStatsDTO(Long totalOrders, BigDecimal totalSpent, Long favoriteProducts, Integer memberDays) {
        this.totalOrders = totalOrders != null ? totalOrders : 0L;
        this.totalSpent = totalSpent != null ? totalSpent : BigDecimal.ZERO;
        this.favoriteProducts = favoriteProducts != null ? favoriteProducts : 0L;
        this.memberDays = memberDays != null ? memberDays : 0;
    }
}