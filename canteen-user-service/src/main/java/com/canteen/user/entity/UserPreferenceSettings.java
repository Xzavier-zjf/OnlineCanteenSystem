package com.canteen.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户偏好设置实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("user_preference_settings")
public class UserPreferenceSettings {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 语言设置
     */
    @TableField("language")
    private String language;

    /**
     * 主题设置（light/dark）
     */
    @TableField("theme")
    private String theme;

    /**
     * 时区设置
     */
    @TableField("timezone")
    private String timezone;

    /**
     * 自动登录（1=开启，0=关闭）
     */
    @TableField("auto_login")
    private Boolean autoLogin;

    /**
     * 分页大小
     */
    @TableField("page_size")
    private Integer pageSize;

    /**
     * 默认支付方式
     */
    @TableField("default_payment")
    private String defaultPayment;

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