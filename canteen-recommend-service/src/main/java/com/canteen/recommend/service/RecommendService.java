package com.canteen.recommend.service;

import com.canteen.recommend.entity.RecommendProduct;
import com.canteen.recommend.entity.UserBehavior;

import java.util.List;

/**
 * 推荐服务接口
 */
public interface RecommendService {

    /**
     * 获取个性化推荐商品
     */
    List<RecommendProduct> getPersonalizedRecommendations(Long userId, Integer limit);

    /**
     * 获取热门推荐商品
     */
    List<RecommendProduct> getHotRecommendations(Integer limit);

    /**
     * 获取相似商品推荐
     */
    List<RecommendProduct> getSimilarProducts(Long productId, Integer limit);

    /**
     * 记录用户行为
     */
    boolean recordUserBehavior(UserBehavior behavior);

    // Controller需要的方法
    List<java.util.Map<String, Object>> getHotRecommend(Integer limit);
    List<java.util.Map<String, Object>> getPersonalRecommend(Long userId, Integer limit);
    List<java.util.Map<String, Object>> getNewRecommend(Integer limit);
}