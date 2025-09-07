package com.canteen.user.controller;

import com.canteen.common.result.Result;
import com.canteen.common.utils.JwtUtils;
import com.canteen.user.dto.UserDTO;
import com.canteen.user.entity.User;
import com.canteen.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 用户控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class UserController {

    private final UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<Object> register(@RequestBody UserDTO.RegisterRequest request) {
        try {
            log.info("收到注册请求: {}", request.getUsername());
            boolean success = userService.register(request);
            if (success) {
                // 返回用户基本信息
                User user = userService.findByUsername(request.getUsername());
                UserDTO.UserInfoResponse userInfo = new UserDTO.UserInfoResponse();
                userInfo.setId(user.getId());
                userInfo.setUsername(user.getUsername());
                userInfo.setPhone(user.getPhone());
                userInfo.setStatus(user.getStatus());
                userInfo.setRole(user.getRole());
                return Result.success("注册成功", userInfo);
            } else {
                return Result.error("注册失败");
            }
        } catch (Exception e) {
            log.error("注册失败: {}", e.getMessage(), e);
            return Result.error("注册失败: " + e.getMessage());
        }
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<UserDTO.LoginResponse> login(@RequestBody UserDTO.LoginRequest request) {
        try {
            log.info("收到登录请求: {}", request.getUsername());
            UserDTO.LoginResponse response = userService.login(request);
            return Result.success("登录成功", response);
        } catch (Exception e) {
            log.error("登录失败: {}", e.getMessage(), e);
            return Result.error("登录失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/info")
    public Result<UserDTO.UserInfoResponse> getUserInfo(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        UserDTO.UserInfoResponse userInfo = userService.getUserInfo(userId);
        return Result.success(userInfo);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/info")
    public Result<Boolean> updateUserInfo(HttpServletRequest request, 
                                         @Validated @RequestBody UserDTO.UpdateUserRequest updateRequest) {
        Long userId = getUserIdFromToken(request);
        boolean success = userService.updateUserInfo(userId, updateRequest);
        return Result.success("更新成功", success);
    }
    
    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    public Result<Boolean> changePassword(HttpServletRequest request,
                                         @Validated @RequestBody UserDTO.ChangePasswordRequest changeRequest) {
        try {
            Long userId = getUserIdFromToken(request);
            boolean success = userService.changePassword(userId, changeRequest);
            return Result.success("密码修改成功", success);
        } catch (Exception e) {
            log.error("密码修改失败: {}", e.getMessage(), e);
            return Result.error("密码修改失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户统计数据
     */
    @GetMapping("/stats")
    public Result<UserDTO.UserStatsResponse> getUserStats(HttpServletRequest request) {
        try {
            Long userId = getUserIdFromToken(request);
            UserDTO.UserStatsResponse stats = userService.getUserStats(userId);
            return Result.success("获取统计数据成功", stats);
        } catch (Exception e) {
            log.error("获取用户统计数据失败: {}", e.getMessage(), e);
            return Result.error("获取统计数据失败: " + e.getMessage());
        }
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public Result<Boolean> logout() {
        // 简单实现，实际项目中可以将token加入黑名单
        return Result.success("登出成功", true);
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("用户服务运行正常");
    }

    /**
     * 获取用户个人资料
     */
    @GetMapping("/profile")
    public Result<UserDTO.UserProfileResponse> getUserProfile(HttpServletRequest request) {
        try {
            Long userId = getUserIdFromToken(request);
            UserDTO.UserProfileResponse profile = userService.getUserProfile(userId);
            return Result.success("获取个人资料成功", profile);
        } catch (Exception e) {
            log.error("获取个人资料失败: {}", e.getMessage(), e);
            return Result.error("获取个人资料失败: " + e.getMessage());
        }
    }

    /**
     * 更新用户个人资料
     */
    @PutMapping("/profile")
    public Result<Boolean> updateUserProfile(HttpServletRequest request,
                                           @RequestBody UserDTO.UpdateProfileRequest updateRequest) {
        try {
            Long userId = getUserIdFromToken(request);
            boolean success = userService.updateUserProfile(userId, updateRequest);
            return Result.success("更新个人资料成功", success);
        } catch (Exception e) {
            log.error("更新个人资料失败: {}", e.getMessage(), e);
            return Result.error("更新个人资料失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户登录记录
     */
    @GetMapping("/login-records")
    public Result<UserDTO.LoginRecordsResponse> getLoginRecords(HttpServletRequest request) {
        try {
            Long userId = getUserIdFromToken(request);
            UserDTO.LoginRecordsResponse records = userService.getLoginRecords(userId);
            return Result.success("获取登录记录成功", records);
        } catch (Exception e) {
            log.error("获取登录记录失败: {}", e.getMessage(), e);
            return Result.error("获取登录记录失败: " + e.getMessage());
        }
    }

    /**
     * 获取通知设置
     */
    @GetMapping("/notification-settings")
    public Result<UserDTO.NotificationSettingsResponse> getNotificationSettings(HttpServletRequest request) {
        try {
            Long userId = getUserIdFromToken(request);
            UserDTO.NotificationSettingsResponse settings = userService.getNotificationSettings(userId);
            return Result.success("获取通知设置成功", settings);
        } catch (Exception e) {
            log.error("获取通知设置失败: {}", e.getMessage(), e);
            return Result.error("获取通知设置失败: " + e.getMessage());
        }
    }

    /**
     * 更新通知设置
     */
    @PutMapping("/notification-settings")
    public Result<Boolean> updateNotificationSettings(HttpServletRequest request,
                                                    @RequestBody UserDTO.UpdateNotificationSettingsRequest updateRequest) {
        try {
            Long userId = getUserIdFromToken(request);
            boolean success = userService.updateNotificationSettings(userId, updateRequest);
            return Result.success("更新通知设置成功", success);
        } catch (Exception e) {
            log.error("更新通知设置失败: {}", e.getMessage(), e);
            return Result.error("更新通知设置失败: " + e.getMessage());
        }
    }

    /**
     * 获取偏好设置
     */
    @GetMapping("/preference-settings")
    public Result<UserDTO.PreferenceSettingsResponse> getPreferenceSettings(HttpServletRequest request) {
        try {
            Long userId = getUserIdFromToken(request);
            UserDTO.PreferenceSettingsResponse settings = userService.getPreferenceSettings(userId);
            return Result.success("获取偏好设置成功", settings);
        } catch (Exception e) {
            log.error("获取偏好设置失败: {}", e.getMessage(), e);
            return Result.error("获取偏好设置失败: " + e.getMessage());
        }
    }

    /**
     * 更新偏好设置
     */
    @PutMapping("/preference-settings")
    public Result<Boolean> updatePreferenceSettings(HttpServletRequest request,
                                                  @RequestBody UserDTO.UpdatePreferenceSettingsRequest updateRequest) {
        try {
            Long userId = getUserIdFromToken(request);
            boolean success = userService.updatePreferenceSettings(userId, updateRequest);
            return Result.success("更新偏好设置成功", success);
        } catch (Exception e) {
            log.error("更新偏好设置失败: {}", e.getMessage(), e);
            return Result.error("更新偏好设置失败: " + e.getMessage());
        }
    }

    /**
     * 简单测试接口
     */
    @PostMapping("/test")
    public Result<String> test(@RequestBody String body) {
        log.info("收到测试请求: {}", body);
        return Result.success("测试成功", "收到数据: " + body);
    }

    /**
     * 从请求头中获取用户ID
     */
    private Long getUserIdFromToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            return JwtUtils.getUserIdFromToken(token);
        }
        throw new RuntimeException("Token无效");
    }
}