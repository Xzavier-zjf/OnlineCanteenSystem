package com.canteen.recommend.controller;

import com.canteen.common.result.Result;
import com.canteen.recommend.entity.RecommendProduct;
import com.canteen.recommend.entity.UserBehavior;
import com.canteen.recommend.service.RecommendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 推荐控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/recommend")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
@RequiredArgsConstructor
public class RecommendControllerNew {

    private final RecommendService recommendService;

    /**
     * 获取个性化推荐
     */
    @GetMapping("/products/{userId}")
    public Result<List<RecommendProduct>> getRecommendProducts(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "6") Integer limit) {
        
        try {
            log.info("获取用户 {} 的个性化推荐，数量: {}", userId, limit);
            List<RecommendProduct> recommendations = recommendService.getPersonalizedRecommendations(userId, limit);
            return Result.success(recommendations);
        } catch (Exception e) {
            log.error("获取个性化推荐失败: {}", e.getMessage(), e);
            return Result.error("获取推荐失败: " + e.getMessage());
        }
    }

    /**
     * 获取热门推荐
     */
    @GetMapping("/hot")
    public Result<List<RecommendProduct>> getHotRecommendations(
            @RequestParam(defaultValue = "6") Integer limit) {
        
        try {
            log.info("获取热门推荐，数量: {}", limit);
            List<RecommendProduct> hotRecommendations = recommendService.getHotRecommendations(limit);
            return Result.success(hotRecommendations);
        } catch (Exception e) {
            log.error("获取热门推荐失败: {}", e.getMessage(), e);
            return Result.error("获取热门推荐失败: " + e.getMessage());
        }
    }

    /**
     * 获取相似商品推荐
     */
    @GetMapping("/similar/{productId}")
    public Result<List<RecommendProduct>> getSimilarProducts(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "6") Integer limit) {
        
        try {
            log.info("获取商品 {} 的相似推荐，数量: {}", productId, limit);
            List<RecommendProduct> similarProducts = recommendService.getSimilarProducts(productId, limit);
            return Result.success(similarProducts);
        } catch (Exception e) {
            log.error("获取相似商品推荐失败: {}", e.getMessage(), e);
            return Result.error("获取相似商品推荐失败: " + e.getMessage());
        }
    }

    /**
     * 记录用户行为
     */
    @PostMapping("/behavior")
    public Result<Boolean> recordBehavior(@RequestBody Map<String, Object> behaviorData) {
        try {
            log.info("记录用户行为: {}", behaviorData);
            
            UserBehavior behavior = new UserBehavior();
            behavior.setUserId(Long.valueOf(String.valueOf(behaviorData.get("userId"))));
            behavior.setProductId(Long.valueOf(String.valueOf(behaviorData.get("productId"))));
            behavior.setAction(String.valueOf(behaviorData.get("action")));
            behavior.setTimestamp(LocalDateTime.now());
            behavior.setSessionId(String.valueOf(behaviorData.get("sessionId")));
            
            boolean success = recommendService.recordUserBehavior(behavior);
            return Result.success("行为记录成功", success);
        } catch (Exception e) {
            log.error("记录用户行为失败: {}", e.getMessage(), e);
            return Result.error("记录行为失败: " + e.getMessage());
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