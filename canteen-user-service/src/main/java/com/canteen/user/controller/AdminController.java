package com.canteen.user.controller;

import com.canteen.common.result.Result;
import com.canteen.common.utils.JwtUtils;
import com.canteen.user.dto.AdminDTO;
import com.canteen.user.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 管理员控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class AdminController {

    private final AdminService adminService;

    /**
     * 获取仪表板数据
     */
    @GetMapping("/dashboard")
    public Result<AdminDTO.DashboardResponse> getDashboard(HttpServletRequest request) {
        try {
            Long userId = getUserIdFromToken(request);
            AdminDTO.DashboardResponse dashboard = adminService.getDashboard(userId);
            return Result.success("获取仪表板数据成功", dashboard);
        } catch (Exception e) {
            log.error("获取仪表板数据失败: {}", e.getMessage(), e);
            return Result.error("获取仪表板数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取系统设置
     */
    @GetMapping("/system-settings")
    public Result<AdminDTO.SystemSettingsResponse> getSystemSettings(HttpServletRequest request) {
        try {
            Long userId = getUserIdFromToken(request);
            AdminDTO.SystemSettingsResponse settings = adminService.getSystemSettings(userId);
            return Result.success("获取系统设置成功", settings);
        } catch (Exception e) {
            log.error("获取系统设置失败: {}", e.getMessage(), e);
            return Result.error("获取系统设置失败: " + e.getMessage());
        }
    }

    /**
     * 更新系统设置
     */
    @PutMapping("/system-settings")
    public Result<Boolean> updateSystemSettings(HttpServletRequest request,
                                              @RequestBody AdminDTO.UpdateSystemSettingsRequest updateRequest) {
        try {
            Long userId = getUserIdFromToken(request);
            boolean success = adminService.updateSystemSettings(userId, updateRequest);
            return Result.success("更新系统设置成功", success);
        } catch (Exception e) {
            log.error("更新系统设置失败: {}", e.getMessage(), e);
            return Result.error("更新系统设置失败: " + e.getMessage());
        }
    }

    /**
     * 获取系统统计数据
     */
    @GetMapping("/system-stats")
    public Result<AdminDTO.SystemStatsResponse> getSystemStats(HttpServletRequest request) {
        try {
            Long userId = getUserIdFromToken(request);
            AdminDTO.SystemStatsResponse stats = adminService.getSystemStats(userId);
            return Result.success("获取系统统计数据成功", stats);
        } catch (Exception e) {
            log.error("获取系统统计数据失败: {}", e.getMessage(), e);
            return Result.error("获取系统统计数据失败: " + e.getMessage());
        }
    }

    /**
     * 系统备份
     */
    @PostMapping("/backup")
    public Result<AdminDTO.BackupResponse> backup(HttpServletRequest request) {
        try {
            Long userId = getUserIdFromToken(request);
            AdminDTO.BackupResponse backup = adminService.backup(userId);
            return Result.success("系统备份成功", backup);
        } catch (Exception e) {
            log.error("系统备份失败: {}", e.getMessage(), e);
            return Result.error("系统备份失败: " + e.getMessage());
        }
    }

    /**
     * 清理系统日志
     */
    @PostMapping("/clear-logs")
    public Result<Boolean> clearLogs(HttpServletRequest request) {
        try {
            Long userId = getUserIdFromToken(request);
            boolean success = adminService.clearLogs(userId);
            return Result.success("清理系统日志成功", success);
        } catch (Exception e) {
            log.error("清理系统日志失败: {}", e.getMessage(), e);
            return Result.error("清理系统日志失败: " + e.getMessage());
        }
    }

    /**
     * 获取系统日志
     */
    @GetMapping("/system-logs")
    public Result<AdminDTO.SystemLogsResponse> getSystemLogs(HttpServletRequest request,
                                                           @RequestParam(defaultValue = "1") int page,
                                                           @RequestParam(defaultValue = "20") int size) {
        try {
            Long userId = getUserIdFromToken(request);
            AdminDTO.SystemLogsResponse logs = adminService.getSystemLogs(userId, page, size);
            return Result.success("获取系统日志成功", logs);
        } catch (Exception e) {
            log.error("获取系统日志失败: {}", e.getMessage(), e);
            return Result.error("获取系统日志失败: " + e.getMessage());
        }
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