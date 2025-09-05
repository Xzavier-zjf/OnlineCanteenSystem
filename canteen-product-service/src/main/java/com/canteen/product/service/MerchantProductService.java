package com.canteen.product.service;

import com.canteen.product.dto.MerchantProductDTO;

import java.util.List;
import java.util.Map;

/**
 * 商户商品服务接口
 */
public interface MerchantProductService {

    /**
     * 获取商户商品总数
     */
    Long getMerchantProductCount(Long merchantId);

    /**
     * 获取商户热销商品
     */
    List<Map<String, Object>> getTopProducts(Long merchantId, Integer limit);

    /**
     * 获取商户商品列表
     */
    Map<String, Object> getMerchantProductList(Long merchantId, Integer page, Integer size, String keyword, Integer status);

    /**
     * 添加商品
     */
    void addProduct(Long merchantId, MerchantProductDTO.AddProductRequest request);

    /**
     * 更新商品
     */
    void updateProduct(Long productId, Long merchantId, MerchantProductDTO.UpdateProductRequest request);

    /**
     * 删除商品
     */
    void deleteProduct(Long productId, Long merchantId);

    /**
     * 更新商品状态
     */
    void updateProductStatus(Long productId, Long merchantId, Integer status);

    /**
     * 更新商品库存
     */
    void updateProductStock(Long productId, Long merchantId, Integer stock);

    /**
     * 获取商品详情
     */
    Map<String, Object> getProductDetail(Long productId, Long merchantId);
}