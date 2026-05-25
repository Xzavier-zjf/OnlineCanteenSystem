package com.canteen.user.controller;

import com.canteen.common.result.Result;
import com.canteen.common.utils.AuthContext;
import com.canteen.user.dto.MerchantDTO;
import com.canteen.user.service.MerchantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
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
    public Result<MerchantDTO.DashboardResponse> getDashboard(HttpServletRequest httpRequest, @PathVariable Long merchantId) {
        try {
            merchantId = requireCurrentMerchant(httpRequest, merchantId);
            MerchantDTO.DashboardResponse dashboard = merchantService.getDashboard(merchantId, httpRequest.getHeader("Authorization"));
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
    public Result<MerchantDTO.MerchantInfo> getMerchantInfo(HttpServletRequest httpRequest, @PathVariable Long merchantId) {
        try {
            merchantId = requireCurrentMerchant(httpRequest, merchantId);
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
    public Result<String> updateMerchantInfo(HttpServletRequest httpRequest,
                                           @PathVariable Long merchantId,
                                           @Valid @RequestBody MerchantDTO.UpdateInfoRequest request) {
        try {
            merchantId = requireCurrentMerchant(httpRequest, merchantId);
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
    public Result<String> changePassword(HttpServletRequest httpRequest,
                                       @PathVariable Long merchantId,
                                       @Valid @RequestBody MerchantDTO.ChangePasswordRequest request) {
        try {
            merchantId = requireCurrentMerchant(httpRequest, merchantId);
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
    public Result<MerchantDTO.OrderStatsResponse> getOrderStats(HttpServletRequest httpRequest,
                                                              @PathVariable Long merchantId,
                                                              @RequestParam(required = false) String startDate,
                                                              @RequestParam(required = false) String endDate) {
        try {
            merchantId = requireCurrentMerchant(httpRequest, merchantId);
            MerchantDTO.OrderStatsResponse stats = merchantService.getOrderStats(merchantId, startDate, endDate, httpRequest.getHeader("Authorization"));
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
    public Result<MerchantDTO.FinanceStatsResponse> getFinanceStats(HttpServletRequest httpRequest,
                                                                  @PathVariable Long merchantId,
                                                                  @RequestParam(required = false) String startDate,
                                                                  @RequestParam(required = false) String endDate) {
        try {
            merchantId = requireCurrentMerchant(httpRequest, merchantId);
            MerchantDTO.FinanceStatsResponse stats = merchantService.getFinanceStats(merchantId, startDate, endDate, httpRequest.getHeader("Authorization"));
            return Result.success("获取财务统计成功", stats);
        } catch (Exception e) {
            log.error("获取财务统计失败", e);
            return Result.error("获取财务统计失败");
        }
    }

    /**
     * 获取热门商品
     */
    @GetMapping("/products/top")
    public Result<java.util.List<java.util.Map<String, Object>>> getTopProducts(@RequestParam(defaultValue = "5") Integer limit) {
        try {
            // 调用产品服务获取热门商品
            java.util.List<java.util.Map<String, Object>> topProducts = merchantService.getTopProducts(limit);
            return Result.success("获取热门商品成功", topProducts);
        } catch (Exception e) {
            log.error("获取热门商品失败", e);
            return Result.error("获取热门商品失败");
        }
    }

    /**
     * 获取收入明细
     */
    @GetMapping("/financial/revenue-details")
    public Result<java.util.Map<String, Object>> getRevenueDetails(HttpServletRequest httpRequest,
                                                                  @RequestParam(required = false) String startDate,
                                                                  @RequestParam(required = false) String endDate) {
        try {
            Long merchantId = requireCurrentMerchant(httpRequest);
            java.util.Map<String, Object> revenueDetails = merchantService.getRevenueDetails(merchantId, startDate, endDate, httpRequest.getHeader("Authorization"));
            return Result.success("获取收入明细成功", revenueDetails);
        } catch (Exception e) {
            log.error("获取收入明细失败", e);
            return Result.error("获取收入明细失败");
        }
    }

    /**
     * 导出财务报表
     */
    @GetMapping("/financial/export")
    public ResponseEntity<byte[]> exportFinancialReport(HttpServletRequest httpRequest,
                                                        @RequestParam(required = false) String startDate,
                                                        @RequestParam(required = false) String endDate) {
        Long merchantId = requireCurrentMerchant(httpRequest);
        byte[] report = merchantService.exportFinancialReport(
                merchantId,
                startDate,
                endDate,
                httpRequest.getHeader("Authorization")
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"merchant-financial-report.csv\"")
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .body(report);
    }

    /**
     * 获取店铺设置
     */
    @GetMapping("/shop-settings")
    public Result<java.util.Map<String, Object>> getShopSettings(HttpServletRequest httpRequest) {
        try {
            Long merchantId = requireCurrentMerchant(httpRequest);
            java.util.Map<String, Object> settings = merchantService.getShopSettings(merchantId);
            return Result.success("获取店铺设置成功", settings);
        } catch (Exception e) {
            log.error("获取店铺设置失败", e);
            return Result.error("获取店铺设置失败");
        }
    }

    /**
     * 更新店铺设置
     */
    @PutMapping("/shop-settings")
    public Result<String> updateShopSettings(HttpServletRequest httpRequest,
                                             @RequestBody java.util.Map<String, Object> settings) {
        try {
            Long merchantId = requireCurrentMerchant(httpRequest);
            merchantService.updateShopSettings(merchantId, settings);
            return Result.success("更新店铺设置成功");
        } catch (Exception e) {
            log.error("更新店铺设置失败", e);
            return Result.error("更新店铺设置失败");
        }
    }

    private Long requireCurrentMerchant(HttpServletRequest request, Long merchantId) {
        AuthContext auth = AuthContext.from(request);
        auth.requireRole("MERCHANT");
        if (!auth.getUserId().equals(merchantId)) {
            throw new SecurityException("无权限访问其他商户数据");
        }
        return auth.getUserId();
    }

    private Long requireCurrentMerchant(HttpServletRequest request) {
        AuthContext auth = AuthContext.from(request);
        auth.requireRole("MERCHANT");
        return auth.getUserId();
    }
}
