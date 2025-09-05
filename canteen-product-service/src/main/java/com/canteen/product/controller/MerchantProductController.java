package com.canteen.product.controller;

import com.canteen.common.result.Result;
import com.canteen.product.dto.MerchantProductDTO;
import com.canteen.product.service.MerchantProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 商户商品控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/products/merchant")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class MerchantProductController {

    private final MerchantProductService merchantProductService;

    /**
     * 获取商户商品总数
     */
    @GetMapping("/{merchantId}/count")
    public Long getMerchantProductCount(@PathVariable Long merchantId) {
        return merchantProductService.getMerchantProductCount(merchantId);
    }

    /**
     * 获取商户热销商品
     */
    @GetMapping("/{merchantId}/top")
    public List<Map<String, Object>> getTopProducts(@PathVariable Long merchantId,
                                                   @RequestParam(defaultValue = "5") Integer limit) {
        return merchantProductService.getTopProducts(merchantId, limit);
    }

    /**
     * 获取商户商品列表
     */
    @GetMapping("/{merchantId}/list")
    public Result<Map<String, Object>> getMerchantProductList(@PathVariable Long merchantId,
                                                             @RequestParam(defaultValue = "1") Integer page,
                                                             @RequestParam(defaultValue = "10") Integer size,
                                                             @RequestParam(required = false) String keyword,
                                                             @RequestParam(required = false) Integer status) {
        try {
            Map<String, Object> result = merchantProductService.getMerchantProductList(merchantId, page, size, keyword, status);
            return Result.success("获取商品列表成功", result);
        } catch (Exception e) {
            log.error("获取商户商品列表失败", e);
            return Result.error("获取商品列表失败");
        }
    }

    /**
     * 添加商品
     */
    @PostMapping("/{merchantId}")
    public Result<String> addProduct(@PathVariable Long merchantId,
                                   @Valid @RequestBody MerchantProductDTO.AddProductRequest request) {
        try {
            merchantProductService.addProduct(merchantId, request);
            return Result.success("商品添加成功");
        } catch (Exception e) {
            log.error("添加商品失败", e);
            return Result.error("添加商品失败：" + e.getMessage());
        }
    }

    /**
     * 更新商品
     */
    @PutMapping("/{productId}")
    public Result<String> updateProduct(@PathVariable Long productId,
                                      @RequestParam Long merchantId,
                                      @Valid @RequestBody MerchantProductDTO.UpdateProductRequest request) {
        try {
            merchantProductService.updateProduct(productId, merchantId, request);
            return Result.success("商品更新成功");
        } catch (Exception e) {
            log.error("更新商品失败", e);
            return Result.error("更新商品失败：" + e.getMessage());
        }
    }

    /**
     * 删除商品
     */
    @DeleteMapping("/{productId}")
    public Result<String> deleteProduct(@PathVariable Long productId, @RequestParam Long merchantId) {
        try {
            merchantProductService.deleteProduct(productId, merchantId);
            return Result.success("商品删除成功");
        } catch (Exception e) {
            log.error("删除商品失败", e);
            return Result.error("删除商品失败：" + e.getMessage());
        }
    }

    /**
     * 上架/下架商品
     */
    @PutMapping("/{productId}/status")
    public Result<String> updateProductStatus(@PathVariable Long productId,
                                            @RequestParam Long merchantId,
                                            @RequestParam Integer status) {
        try {
            merchantProductService.updateProductStatus(productId, merchantId, status);
            return Result.success("商品状态更新成功");
        } catch (Exception e) {
            log.error("更新商品状态失败", e);
            return Result.error("更新商品状态失败：" + e.getMessage());
        }
    }

    /**
     * 更新商品库存
     */
    @PutMapping("/{productId}/stock")
    public Result<String> updateProductStock(@PathVariable Long productId,
                                           @RequestParam Long merchantId,
                                           @RequestParam Integer stock) {
        try {
            merchantProductService.updateProductStock(productId, merchantId, stock);
            return Result.success("库存更新成功");
        } catch (Exception e) {
            log.error("更新库存失败", e);
            return Result.error("更新库存失败：" + e.getMessage());
        }
    }

    /**
     * 获取商品详情
     */
    @GetMapping("/{productId}/detail")
    public Result<Map<String, Object>> getProductDetail(@PathVariable Long productId, 
                                                       @RequestParam Long merchantId) {
        try {
            Map<String, Object> productDetail = merchantProductService.getProductDetail(productId, merchantId);
            return Result.success("获取商品详情成功", productDetail);
        } catch (Exception e) {
            log.error("获取商品详情失败", e);
            return Result.error("获取商品详情失败");
        }
    }
}