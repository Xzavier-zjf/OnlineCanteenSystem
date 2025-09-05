package com.canteen.product.controller;

import com.canteen.common.result.Result;
import com.canteen.product.service.AdminProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 管理员商品控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class AdminProductController {

    private final AdminProductService adminProductService;

    /**
     * 获取商品总数
     */
    @GetMapping("/count")
    public Long getProductCount() {
        return adminProductService.getTotalProductCount();
    }

    /**
     * 获取商户商品统计
     */
    @GetMapping("/merchant/{merchantId}/stats")
    public Map<String, Object> getMerchantProductStats(@PathVariable Long merchantId) {
        return adminProductService.getMerchantProductStats(merchantId);
    }

    /**
     * 获取全平台商品列表（管理员）
     */
    @GetMapping("/admin/list")
    public Result<Map<String, Object>> getAdminProductList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        try {
            Map<String, Object> result = adminProductService.getAdminProductList(page, size, keyword, status);
            return Result.success("获取商品列表成功", result);
        } catch (Exception e) {
            log.error("获取管理员商品列表失败", e);
            return Result.error("获取商品列表失败");
        }
    }

    /**
     * 审核商品
     */
    @PutMapping("/{productId}/audit")
    public Result<String> auditProduct(@PathVariable Long productId, 
                                     @RequestParam Boolean approved,
                                     @RequestParam(required = false) String reason) {
        try {
            adminProductService.auditProduct(productId, approved, reason);
            return Result.success("商品审核完成");
        } catch (Exception e) {
            log.error("商品审核失败", e);
            return Result.error("商品审核失败：" + e.getMessage());
        }
    }

    /**
     * 删除违规商品
     */
    @DeleteMapping("/{productId}")
    public Result<String> deleteProduct(@PathVariable Long productId, 
                                      @RequestParam(required = false) String reason) {
        try {
            adminProductService.deleteProduct(productId, reason);
            return Result.success("商品删除成功");
        } catch (Exception e) {
            log.error("删除商品失败", e);
            return Result.error("删除商品失败：" + e.getMessage());
        }
    }

    /**
     * 设置推荐商品
     */
    @PutMapping("/{productId}/recommend")
    public Result<String> setRecommendProduct(@PathVariable Long productId, 
                                            @RequestParam Boolean isRecommend) {
        try {
            adminProductService.setRecommendProduct(productId, isRecommend);
            return Result.success("推荐设置成功");
        } catch (Exception e) {
            log.error("设置推荐商品失败", e);
            return Result.error("设置推荐商品失败：" + e.getMessage());
        }
    }
}