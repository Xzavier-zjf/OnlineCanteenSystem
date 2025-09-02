package com.canteen.recommend.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 推荐商品实体
 */
@Data
public class RecommendProduct {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String imageUrl;
    private Integer sales;
    private Double rating;
    private String reason; // 推荐理由
    private Double score;  // 推荐分数
}