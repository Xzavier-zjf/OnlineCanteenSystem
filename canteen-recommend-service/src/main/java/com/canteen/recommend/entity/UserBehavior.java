package com.canteen.recommend.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户行为实体
 */
@Data
public class UserBehavior {
    private Long id;
    private Long userId;
    private Long productId;
    private String action; // view, add_to_cart, purchase, etc.
    private LocalDateTime timestamp;
    private String sessionId;
}