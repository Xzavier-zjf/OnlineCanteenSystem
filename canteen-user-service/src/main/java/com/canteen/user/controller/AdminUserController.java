package com.canteen.user.controller;

import com.canteen.common.result.Result;
import com.canteen.user.dto.AdminUserDTO;
import com.canteen.user.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 管理员用户管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    /**
     * 获取用户列表（分页）
     */
    @GetMapping
    public Result<Map<String, Object>> getUserList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status) {
        try {
            Map<String, Object> result = adminUserService.getUserList(page, size, keyword, role, status);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取用户列表失败", e);
            return Result.error("获取用户列表失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/{userId}")
    public Result<AdminUserDTO> getUserDetail(@PathVariable Long userId) {
        try {
            AdminUserDTO user = adminUserService.getUserDetail(userId);
            return Result.success(user);
        } catch (Exception e) {
            log.error("获取用户详情失败：userId={}", userId, e);
            return Result.error("获取用户详情失败：" + e.getMessage());
        }
    }

    /**
     * 启用/禁用用户账号
     */
    @PutMapping("/{userId}/status")
    public Result<Void> updateUserStatus(
            @PathVariable Long userId,
            @RequestParam String status) {
        try {
            adminUserService.updateUserStatus(userId, status);
            return Result.success();
        } catch (Exception e) {
            log.error("更新用户状态失败：userId={}, status={}", userId, status, e);
            return Result.error("更新用户状态失败：" + e.getMessage());
        }
    }

    /**
     * 重置用户密码
     */
    @PutMapping("/{userId}/password/reset")
    public Result<String> resetUserPassword(@PathVariable Long userId) {
        try {
            String newPassword = adminUserService.resetUserPassword(userId);
            return Result.success(newPassword);
        } catch (Exception e) {
            log.error("重置用户密码失败：userId={}", userId, e);
            return Result.error("重置用户密码失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户统计信息
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getUserStatistics() {
        try {
            Map<String, Object> stats = adminUserService.getUserStatistics();
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取用户统计信息失败", e);
            return Result.error("获取用户统计信息失败：" + e.getMessage());
        }
    }

    /**
     * 审核商户注册
     */
    @PutMapping("/merchants/{userId}/approve")
    public Result<Void> approveMerchant(
            @PathVariable Long userId,
            @RequestParam Boolean approved,
            @RequestParam(required = false) String reason) {
        try {
            adminUserService.approveMerchant(userId, approved, reason);
            return Result.success();
        } catch (Exception e) {
            log.error("审核商户失败：userId={}, approved={}", userId, approved, e);
            return Result.error("审核商户失败：" + e.getMessage());
        }
    }

    /**
     * 获取待审核商户列表
     */
    @GetMapping("/merchants/pending")
    public Result<Map<String, Object>> getPendingMerchants(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        try {
            Map<String, Object> result = adminUserService.getPendingMerchants(page, size);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取待审核商户列表失败", e);
            return Result.error("获取待审核商户列表失败：" + e.getMessage());
        }
    }
}