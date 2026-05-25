package com.canteen.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户通知设置实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("user_notification_settings")
public class UserNotificationSettings {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 邮件通知（1=开启，0=关闭）
     */
    @TableField("email_notification")
    private Boolean emailNotification;

    /**
     * 短信通知（1=开启，0=关闭）
     */
    @TableField("sms_notification")
    private Boolean smsNotification;

    /**
     * 订单通知（1=开启，0=关闭）
     */
    @TableField("order_notification")
    private Boolean orderNotification;

    /**
     * 促销通知（1=开启，0=关闭）
     */
    @TableField("promotion_notification")
    private Boolean promotionNotification;

    /**
     * 系统通知（1=开启，0=关闭）
     */
    @TableField("system_notification")
    private Boolean systemNotification;

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