package com.canteen.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商户设置实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("merchant_settings")
public class MerchantSettings {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 商户用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 店铺名称
     */
    @TableField("shop_name")
    private String shopName;

    /**
     * 店铺描述
     */
    @TableField("shop_description")
    private String shopDescription;

    /**
     * 店铺Logo
     */
    @TableField("shop_logo")
    private String shopLogo;

    /**
     * 营业时间（JSON格式）
     */
    @TableField("business_hours")
    private String businessHours;

    /**
     * 联系电话
     */
    @TableField("contact_phone")
    private String contactPhone;

    /**
     * 店铺地址
     */
    @TableField("shop_address")
    private String shopAddress;

    /**
     * 配送费
     */
    @TableField("delivery_fee")
    private BigDecimal deliveryFee;

    /**
     * 起送金额
     */
    @TableField("min_order_amount")
    private BigDecimal minOrderAmount;

    /**
     * 自动接单（1=开启，0=关闭）
     */
    @TableField("auto_accept_order")
    private Boolean autoAcceptOrder;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}