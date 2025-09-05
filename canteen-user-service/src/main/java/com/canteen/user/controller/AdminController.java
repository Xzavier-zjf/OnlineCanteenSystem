package com.canteen.user.controller;

import com.canteen.common.result.Result;
import com.canteen.user.dto.AdminDTO;
import com.canteen.user.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

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
     * 管理员登录
     */
    @PostMapping("/login")
    public Result<AdminDTO.LoginResponse> login(@Valid @RequestBody AdminDTO.LoginRequest request) {
        try {
            AdminDTO.LoginResponse response = adminService.login(request);
            return Result.success("登录成功", response);
        } catch (Exception e) {
            log.error("管理员登录失败", e);
            return Result.error("登录失败：" + e.getMessage());
        }
    }

    /**
     * 获取管理员仪表板数据
     */
    @GetMapping("/dashboard")
    public Result<AdminDTO.DashboardResponse> getDashboard() {
        try {
            AdminDTO.DashboardResponse dashboard = adminService.getDashboard();
            return Result.success("获取仪表板数据成功", dashboard);
        } catch (Exception e) {
            log.error("获取仪表板数据失败", e);
            return Result.error("获取仪表板数据失败");
        }
    }

    /**
     * 用户管理 - 获取用户列表 (已移至AdminUserController)
     * 此方法已废弃，请使用 /api/admin/users
     */
    @Deprecated
    @GetMapping("/user-list")
    public Result<AdminDTO.PageResponse<AdminDTO.UserInfo>> getUsers(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role) {
        try {
            AdminDTO.PageResponse<AdminDTO.UserInfo> users = adminService.getUsers(page, size, keyword, role);
            return Result.success("获取用户列表成功", users);
        } catch (Exception e) {
            log.error("获取用户列表失败", e);
            return Result.error("获取用户列表失败");
        }
    }

    /**
     * 用户管理 - 更新用户状态 (已移至AdminUserController)
     * 此方法已废弃，请使用 /api/admin/users/{userId}/status
     */
    @Deprecated
    @PutMapping("/user/{userId}/status")
    public Result<String> updateUserStatus(@PathVariable Long userId, @RequestBody AdminDTO.UpdateStatusRequest request) {
        try {
            adminService.updateUserStatus(userId, request.getStatus());
            return Result.success("用户状态更新成功");
        } catch (Exception e) {
            log.error("更新用户状态失败", e);
            return Result.error("更新用户状态失败");
        }
    }

    /**
     * 用户管理 - 重置用户密码 (已移至AdminUserController)
     * 此方法已废弃，请使用 /api/admin/users/{userId}/password/reset
     */
    @Deprecated
    @PutMapping("/user/{userId}/password/reset")
    public Result<String> resetUserPassword(@PathVariable Long userId) {
        try {
            String newPassword = adminService.resetUserPassword(userId);
            return Result.success("密码重置成功，新密码：" + newPassword);
        } catch (Exception e) {
            log.error("重置用户密码失败", e);
            return Result.error("重置用户密码失败");
        }
    }

    /**
     * 商户管理 - 获取商户列表
     */
    @GetMapping("/merchants")
    public Result<AdminDTO.PageResponse<AdminDTO.MerchantInfo>> getMerchants(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        try {
            AdminDTO.PageResponse<AdminDTO.MerchantInfo> merchants = adminService.getMerchants(page, size, keyword);
            return Result.success("获取商户列表成功", merchants);
        } catch (Exception e) {
            log.error("获取商户列表失败", e);
            return Result.error("获取商户列表失败");
        }
    }

    /**
     * 商户管理 - 审核商户
     */
    @PutMapping("/merchants/{merchantId}/approve")
    public Result<String> approveMerchant(@PathVariable Long merchantId, @RequestBody AdminDTO.ApproveRequest request) {
        try {
            adminService.approveMerchant(merchantId, request.getApproved(), request.getReason());
            return Result.success("商户审核完成");
        } catch (Exception e) {
            log.error("商户审核失败", e);
            return Result.error("商户审核失败");
        }
    }
}