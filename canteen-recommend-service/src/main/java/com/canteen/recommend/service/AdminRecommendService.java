package com.canteen.recommend.service;

import java.util.List;
import java.util.Map;

/**
 * 管理员推荐服务接口
 */
public interface AdminRecommendService {

    /**
     * 获取推荐商品列表
     */
    List<Map<String, Object>> getRecommendProducts();

    /**
     * 设置推荐商品
     */
    void setRecommendProducts(List<Long> productIds);

    /**
     * 获取热销商品列表
     */
    List<Map<String, Object>> getHotProducts();

    /**
     * 设置热销商品
     */
    void setHotProducts(List<Long> productIds);

    /**
     * 获取推荐统计信息
     */
    Map<String, Object> getRecommendStatistics();
}