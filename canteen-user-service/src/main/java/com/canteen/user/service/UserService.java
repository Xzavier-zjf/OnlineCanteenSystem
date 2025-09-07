package com.canteen.user.service;

import com.canteen.user.dto.UserDTO;
import com.canteen.user.entity.User;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 用户注册
     * @param request 注册请求
     * @return 注册结果
     */
    boolean register(UserDTO.RegisterRequest request);

    /**
     * 用户登录
     * @param request 登录请求
     * @return 登录响应
     */
    UserDTO.LoginResponse login(UserDTO.LoginRequest request);

    /**
     * 根据用户ID获取用户信息
     * @param userId 用户ID
     * @return 用户信息
     */
    UserDTO.UserInfoResponse getUserInfo(Long userId);

    /**
     * 更新用户信息
     * @param userId 用户ID
     * @param request 更新请求
     * @return 更新结果
     */
    boolean updateUserInfo(Long userId, UserDTO.UpdateUserRequest request);

    /**
     * 根据用户名查找用户
     * @param username 用户名
     * @return 用户信息
     */
    User findByUsername(String username);

    /**
     * 验证密码
     * @param rawPassword 原始密码
     * @param encodedPassword 加密密码
     * @return 验证结果
     */
    boolean verifyPassword(String rawPassword, String encodedPassword);
    
    /**
     * 修改密码
     * @param userId 用户ID
     * @param request 修改密码请求
     * @return 修改结果
     */
    boolean changePassword(Long userId, UserDTO.ChangePasswordRequest request);
    
    /**
     * 获取用户统计数据
     * @param userId 用户ID
     * @return 用户统计数据
     */
    UserDTO.UserStatsResponse getUserStats(Long userId);

    /**
     * 获取用户个人资料
     * @param userId 用户ID
     * @return 用户个人资料
     */
    UserDTO.UserProfileResponse getUserProfile(Long userId);

    /**
     * 更新用户个人资料
     * @param userId 用户ID
     * @param request 更新请求
     * @return 更新结果
     */
    boolean updateUserProfile(Long userId, UserDTO.UpdateProfileRequest request);

    /**
     * 获取用户登录记录
     * @param userId 用户ID
     * @return 登录记录
     */
    UserDTO.LoginRecordsResponse getLoginRecords(Long userId);

    /**
     * 获取通知设置
     * @param userId 用户ID
     * @return 通知设置
     */
    UserDTO.NotificationSettingsResponse getNotificationSettings(Long userId);

    /**
     * 更新通知设置
     * @param userId 用户ID
     * @param request 更新请求
     * @return 更新结果
     */
    boolean updateNotificationSettings(Long userId, UserDTO.UpdateNotificationSettingsRequest request);

    /**
     * 获取偏好设置
     * @param userId 用户ID
     * @return 偏好设置
     */
    UserDTO.PreferenceSettingsResponse getPreferenceSettings(Long userId);

    /**
     * 更新偏好设置
     * @param userId 用户ID
     * @param request 更新请求
     * @return 更新结果
     */
    boolean updatePreferenceSettings(Long userId, UserDTO.UpdatePreferenceSettingsRequest request);
}
