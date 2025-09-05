package com.canteen.recommend.controller;

import com.canteen.common.result.Result;
import com.canteen.recommend.service.AdminRecommendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 管理员推荐管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/recommend")
@RequiredArgsConstructor
public class AdminRecommendController {

    private final AdminRecommendService adminRecommendService;

    /**
     * 获取推荐商品列表
     */
    @GetMapping("/products")
    public Result<List<Map<String, Object>>> getRecommendProducts() {
        try {
            List<Map<String, Object>> products = adminRecommendService.getRecommendProducts();
            return Result.success(products);
        } catch (Exception e) {
            log.error("获取推荐商品列表失败", e);
            return Result.error("获取推荐商品列表失败：" + e.getMessage());
        }
    }

    /**
     * 设置推荐商品
     */
    @PostMapping("/products")
    public Result<Void> setRecommendProducts(@RequestBody List<Long> productIds) {
        try {
            adminRecommendService.setRecommendProducts(productIds);
            return Result.success();
        } catch (Exception e) {
            log.error("设置推荐商品失败", e);
            return Result.error("设置推荐商品失败：" + e.getMessage());
        }
    }

    /**
     * 获取热销商品列表
     */
    @GetMapping("/hot")
    public Result<List<Map<String, Object>>> getHotProducts() {
        try {
            List<Map<String, Object>> products = adminRecommendService.getHotProducts();
            return Result.success(products);
        } catch (Exception e) {
            log.error("获取热销商品列表失败", e);
            return Result.error("获取热销商品列表失败：" + e.getMessage());
        }
    }

    /**
     * 设置热销商品
     */
    @PostMapping("/hot")
    public Result<Void> setHotProducts(@RequestBody List<Long> productIds) {
        try {
            adminRecommendService.setHotProducts(productIds);
            return Result.success();
        } catch (Exception e) {
            log.error("设置热销商品失败", e);
            return Result.error("设置热销商品失败：" + e.getMessage());
        }
    }

    /**
     * 获取推荐统计信息
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getRecommendStatistics() {
        try {
            Map<String, Object> stats = adminRecommendService.getRecommendStatistics();
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取推荐统计信息失败", e);
            return Result.error("获取推荐统计信息失败：" + e.getMessage());
        }
    }
}