package com.canteen.user.service;

import com.canteen.user.dto.AdminDTO;

/**
 * 管理员服务接口
 */
public interface AdminService {

    /**
     * 获取仪表板数据
     * @param userId 用户ID
     * @return 仪表板数据
     */
    AdminDTO.DashboardResponse getDashboard(Long userId);

    /**
     * 获取系统设置
     * @param userId 用户ID
     * @return 系统设置
     */
    AdminDTO.SystemSettingsResponse getSystemSettings(Long userId);

    /**
     * 更新系统设置
     * @param userId 用户ID
     * @param request 更新请求
     * @return 更新结果
     */
    boolean updateSystemSettings(Long userId, AdminDTO.UpdateSystemSettingsRequest request);

    /**
     * 获取系统统计数据
     * @param userId 用户ID
     * @return 系统统计数据
     */
    AdminDTO.SystemStatsResponse getSystemStats(Long userId);

    /**
     * 系统备份
     * @param userId 用户ID
     * @return 备份结果
     */
    AdminDTO.BackupResponse backup(Long userId);

    /**
     * 清理系统日志
     * @param userId 用户ID
     * @return 清理结果
     */
    boolean clearLogs(Long userId);

    /**
     * 获取系统日志
     * @param userId 用户ID
     * @param page 页码
     * @param size 页大小
     * @return 系统日志
     */
    AdminDTO.SystemLogsResponse getSystemLogs(Long userId, int page, int size);
}