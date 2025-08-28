package com.canteen.recommend.controller;

import com.canteen.common.result.Result;
import com.canteen.recommend.service.RecommendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 推荐服务控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/recommend")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
@RequiredArgsConstructor
public class RecommendController {

    private final RecommendService recommendService;

    /**
     * 获取热门推荐
     */
    @GetMapping("/hot")
    public Result<List<Map<String, Object>>> getHotRecommend(
            @RequestParam(defaultValue = "5") Integer limit) {
        
        try {
            List<Map<String, Object>> recommendations = recommendService.getHotRecommend(limit);
            return Result.success("热门推荐获取成功", recommendations);
        } catch (Exception e) {
            log.error("获取热门推荐失败", e);
            return Result.error("获取热门推荐失败");
        }
    }

    /**
     * 获取个性化推荐
     */
    @GetMapping("/personal/{userId}")
    public Result<List<Map<String, Object>>> getPersonalRecommend(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "5") Integer limit) {
        
        try {
            List<Map<String, Object>> recommendations = recommendService.getPersonalRecommend(userId, limit);
            return Result.success("个性化推荐获取成功", recommendations);
        } catch (Exception e) {
            log.error("获取个性化推荐失败", e);
            return Result.error("获取个性化推荐失败");
        }
    }

    /**
     * 获取用户推荐产品（兼容前端API调用）
     */
    @GetMapping("/products/{userId}")
    public Result<List<Map<String, Object>>> getRecommendProducts(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "6") Integer limit) {
        
        try {
            List<Map<String, Object>> recommendations = recommendService.getPersonalRecommend(userId, limit);
            return Result.success("推荐产品获取成功", recommendations);
        } catch (Exception e) {
            log.error("获取推荐产品失败", e);
            return Result.error("获取推荐产品失败");
        }
    }

    /**
     * 获取相似产品推荐
     */
    @GetMapping("/similar/{productId}")
    public Result<List<Map<String, Object>>> getSimilarProducts(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "6") Integer limit) {
        
        try {
            List<Map<String, Object>> recommendations = recommendService.getSimilarProducts(productId, limit);
            return Result.success("相似产品推荐获取成功", recommendations);
        } catch (Exception e) {
            log.error("获取相似产品推荐失败", e);
            return Result.error("获取相似产品推荐失败");
        }
    }

    /**
     * 获取新品推荐
     */
    @GetMapping("/new")
    public Result<List<Map<String, Object>>> getNewRecommend(
            @RequestParam(defaultValue = "5") Integer limit) {
        
        try {
            List<Map<String, Object>> recommendations = recommendService.getNewRecommend(limit);
            return Result.success("新品推荐获取成功", recommendations);
        } catch (Exception e) {
            log.error("获取新品推荐失败", e);
            return Result.error("获取新品推荐失败");
        }
    }

    /**
     * 记录用户行为
     */
    @PostMapping("/behavior")
    public Result<String> recordBehavior(@RequestBody Map<String, Object> behaviorData) {
        try {
            log.info("记录用户行为: {}", behaviorData);
            // 这里可以实现具体的行为记录逻辑
            // 目前只是简单记录日志
            return Result.success("行为记录成功");
        } catch (Exception e) {
            log.error("记录用户行为失败", e);
            return Result.error("记录用户行为失败");
        }
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("推荐服务运行正常");
    }
}