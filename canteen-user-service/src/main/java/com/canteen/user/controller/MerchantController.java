package com.canteen.user.controller;

import com.canteen.common.result.Result;
import com.canteen.user.dto.MerchantDTO;
import com.canteen.user.service.MerchantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 商户控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/merchant")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class MerchantController {

    private final MerchantService merchantService;

    /**
     * 商户登录
     */
    @PostMapping("/login")
    public Result<MerchantDTO.LoginResponse> login(@Valid @RequestBody MerchantDTO.LoginRequest request) {
        try {
            MerchantDTO.LoginResponse response = merchantService.login(request);
            return Result.success("登录成功", response);
        } catch (Exception e) {
            log.error("商户登录失败", e);
            return Result.error("登录失败：" + e.getMessage());
        }
    }

    /**
     * 商户注册
     */
    @PostMapping("/register")
    public Result<String> register(@Valid @RequestBody MerchantDTO.RegisterRequest request) {
        try {
            merchantService.register(request);
            return Result.success("注册成功，请等待管理员审核");
        } catch (Exception e) {
            log.error("商户注册失败", e);
            return Result.error("注册失败：" + e.getMessage());
        }
    }

    /**
     * 获取商户仪表板数据
     */
    @GetMapping("/dashboard/{merchantId}")
    public Result<MerchantDTO.DashboardResponse> getDashboard(@PathVariable Long merchantId) {
        try {
            MerchantDTO.DashboardResponse dashboard = merchantService.getDashboard(merchantId);
            return Result.success("获取仪表板数据成功", dashboard);
        } catch (Exception e) {
            log.error("获取商户仪表板数据失败", e);
            return Result.error("获取仪表板数据失败");
        }
    }

    /**
     * 获取商户信息
     */
    @GetMapping("/info/{merchantId}")
    public Result<MerchantDTO.MerchantInfo> getMerchantInfo(@PathVariable Long merchantId) {
        try {
            MerchantDTO.MerchantInfo merchantInfo = merchantService.getMerchantInfo(merchantId);
            return Result.success("获取商户信息成功", merchantInfo);
        } catch (Exception e) {
            log.error("获取商户信息失败", e);
            return Result.error("获取商户信息失败");
        }
    }

    /**
     * 更新商户信息
     */
    @PutMapping("/info/{merchantId}")
    public Result<String> updateMerchantInfo(@PathVariable Long merchantId, 
                                           @Valid @RequestBody MerchantDTO.UpdateInfoRequest request) {
        try {
            merchantService.updateMerchantInfo(merchantId, request);
            return Result.success("商户信息更新成功");
        } catch (Exception e) {
            log.error("更新商户信息失败", e);
            return Result.error("更新商户信息失败");
        }
    }

    /**
     * 修改密码
     */
    @PutMapping("/password/{merchantId}")
    public Result<String> changePassword(@PathVariable Long merchantId, 
                                       @Valid @RequestBody MerchantDTO.ChangePasswordRequest request) {
        try {
            merchantService.changePassword(merchantId, request);
            return Result.success("密码修改成功");
        } catch (Exception e) {
            log.error("修改密码失败", e);
            return Result.error("修改密码失败");
        }
    }

    /**
     * 获取商户订单统计
     */
    @GetMapping("/orders/stats/{merchantId}")
    public Result<MerchantDTO.OrderStatsResponse> getOrderStats(@PathVariable Long merchantId,
                                                              @RequestParam(required = false) String startDate,
                                                              @RequestParam(required = false) String endDate) {
        try {
            MerchantDTO.OrderStatsResponse stats = merchantService.getOrderStats(merchantId, startDate, endDate);
            return Result.success("获取订单统计成功", stats);
        } catch (Exception e) {
            log.error("获取订单统计失败", e);
            return Result.error("获取订单统计失败");
        }
    }

    /**
     * 获取商户财务统计
     */
    @GetMapping("/finance/stats/{merchantId}")
    public Result<MerchantDTO.FinanceStatsResponse> getFinanceStats(@PathVariable Long merchantId,
                                                                  @RequestParam(required = false) String startDate,
                                                                  @RequestParam(required = false) String endDate) {
        try {
            MerchantDTO.FinanceStatsResponse stats = merchantService.getFinanceStats(merchantId, startDate, endDate);
            return Result.success("获取财务统计成功", stats);
        } catch (Exception e) {
            log.error("获取财务统计失败", e);
            return Result.error("获取财务统计失败");
        }
    }
}