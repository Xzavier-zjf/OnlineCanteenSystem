package com.canteen.user.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * 用户相关请求DTO
 */
public class UserDTO {

    /**
     * 用户注册请求
     */
    @Data
    public static class RegisterRequest {
        @NotBlank(message = "用户名不能为空")
        @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
        private String username;

        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 100, message = "密码长度必须在6-100个字符之间")
        private String password;

        @Email(message = "邮箱格式不正确")
        private String email;

        private String phone;

        private String realName;
    }

    /**
     * 用户登录请求
     */
    @Data
    public static class LoginRequest {
        @NotBlank(message = "用户名不能为空")
        private String username;

        @NotBlank(message = "密码不能为空")
        private String password;
    }

    /**
     * 用户登录响应
     */
    @Data
    public static class LoginResponse {
        private Long userId;
        private String username;
        private String realName;
        private String role;
        private String token;
    }

    /**
     * 用户信息响应
     */
    @Data
    public static class UserInfoResponse {
        private Long id;
        private String username;
        private String email;
        private String phone;
        private String realName;
        private String college;
        private String address;
        private String role;
        private Integer status;
        private LocalDateTime createTime;  // 注册时间
        private LocalDateTime updateTime;  // 更新时间
    }

    /**
     * 更新用户信息请求
     */
    @Data
    public static class UpdateUserRequest {
        @Email(message = "邮箱格式不正确")
        private String email;

        private String phone;

        private String realName;
        
        private String college;
        
        private String address;
    }
    
    /**
     * 修改密码请求
     */
    @Data
    public static class ChangePasswordRequest {
        @NotBlank(message = "原密码不能为空")
        private String oldPassword;

        @NotBlank(message = "新密码不能为空")
        @Size(min = 6, max = 100, message = "密码长度必须在6-100个字符之间")
        private String newPassword;
    }

    /**
     * 用户统计数据响应
     */
    @Data
    public static class UserStatsResponse {
        private Integer totalOrders;      // 总订单数
        private String totalAmount;       // 总消费金额
        private Integer favoriteCount;    // 收藏商品数
    }
}
