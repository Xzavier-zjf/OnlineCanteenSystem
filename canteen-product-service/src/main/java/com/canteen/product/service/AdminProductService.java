package com.canteen.product.service;

import java.util.Map;

/**
 * 管理员商品服务接口
 */
public interface AdminProductService {

    /**
     * 获取商品总数
     */
    Long getTotalProductCount();

    /**
     * 获取商户商品统计
     */
    Map<String, Object> getMerchantProductStats(Long merchantId);

    /**
     * 获取管理员商品列表
     */
    Map<String, Object> getAdminProductList(Integer page, Integer size, String keyword, Integer status);

    /**
     * 审核商品
     */
    void auditProduct(Long productId, Boolean approved, String reason);

    /**
     * 删除商品
     */
    void deleteProduct(Long productId, String reason);

    /**
     * 设置推荐商品
     */
    void setRecommendProduct(Long productId, Boolean isRecommend);
}