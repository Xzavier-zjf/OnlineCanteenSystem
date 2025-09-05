package com.canteen.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.canteen.product.entity.Product;
import com.canteen.product.mapper.ProductMapper;
import com.canteen.product.service.AdminProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 管理员商品服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminProductServiceImpl implements AdminProductService {

    private final ProductMapper productMapper;

    @Override
    public Long getTotalProductCount() {
        try {
            return productMapper.selectCount(null);
        } catch (Exception e) {
            log.error("获取商品总数失败", e);
            return 0L;
        }
    }

    @Override
    public Map<String, Object> getMerchantProductStats(Long merchantId) {
        try {
            Map<String, Object> stats = new HashMap<>();

            // 商户商品总数
            QueryWrapper<Product> totalWrapper = new QueryWrapper<>();
            totalWrapper.eq("merchant_id", merchantId);
            Long totalProducts = productMapper.selectCount(totalWrapper);
            stats.put("totalProducts", totalProducts);

            // 商户订单总数（需要调用订单服务获取）
            stats.put("totalOrders", 0L);

            // 商户总销售额（需要调用订单服务获取）
            stats.put("totalSales", 0L);

            return stats;
        } catch (Exception e) {
            log.error("获取商户商品统计失败：merchantId={}", merchantId, e);
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalProducts", 0L);
            stats.put("totalOrders", 0L);
            stats.put("totalSales", 0L);
            return stats;
        }
    }

    @Override
    public Map<String, Object> getAdminProductList(Integer page, Integer size, String keyword, Integer status) {
        try {
            Page<Product> pageParam = new Page<>(page, size);
            QueryWrapper<Product> wrapper = new QueryWrapper<>();

            // 关键词搜索
            if (StringUtils.hasText(keyword)) {
                wrapper.and(w -> w.like("name", keyword)
                               .or().like("description", keyword));
            }

            // 状态筛选
            if (status != null) {
                wrapper.eq("status", status);
            }

            wrapper.orderByDesc("create_time");

            Page<Product> productPage = productMapper.selectPage(pageParam, wrapper);

            // 构建返回结果
            List<Map<String, Object>> productList = productPage.getRecords().stream()
                    .map(this::convertProductToMap)
                    .collect(Collectors.toList());

            Map<String, Object> result = new HashMap<>();
            result.put("records", productList);
            result.put("total", productPage.getTotal());
            result.put("page", page);
            result.put("size", size);
            result.put("pages", productPage.getPages());

            return result;
        } catch (Exception e) {
            log.error("获取管理员商品列表失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("records", new ArrayList<>());
            result.put("total", 0L);
            result.put("page", page);
            result.put("size", size);
            result.put("pages", 0L);
            return result;
        }
    }

    @Override
    @Transactional
    public void auditProduct(Long productId, Boolean approved, String reason) {
        try {
            Product product = productMapper.selectById(productId);
            if (product == null) {
                throw new RuntimeException("商品不存在");
            }

            // 更新商品状态
            product.setStatus(approved ? 1 : 0);
            product.setUpdateTime(LocalDateTime.now());
            productMapper.updateById(product);

            // 这里可以记录审核日志到product_audit表
            log.info("商品审核完成：productId={}, approved={}, reason={}", productId, approved, reason);
        } catch (Exception e) {
            log.error("商品审核失败：productId={}", productId, e);
            throw new RuntimeException("商品审核失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void deleteProduct(Long productId, String reason) {
        try {
            Product product = productMapper.selectById(productId);
            if (product == null) {
                throw new RuntimeException("商品不存在");
            }

            // 软删除：设置状态为禁用
            product.setStatus(0);
            product.setUpdateTime(LocalDateTime.now());
            productMapper.updateById(product);

            log.info("商品删除成功：productId={}, reason={}", productId, reason);
        } catch (Exception e) {
            log.error("删除商品失败：productId={}", productId, e);
            throw new RuntimeException("删除商品失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void setRecommendProduct(Long productId, Boolean isRecommend) {
        try {
            Product product = productMapper.selectById(productId);
            if (product == null) {
                throw new RuntimeException("商品不存在");
            }

            // 更新热门标识
            product.setIsHot(isRecommend ? 1 : 0);
            product.setUpdateTime(LocalDateTime.now());
            productMapper.updateById(product);

            log.info("推荐商品设置成功：productId={}, isRecommend={}", productId, isRecommend);
        } catch (Exception e) {
            log.error("设置推荐商品失败：productId={}", productId, e);
            throw new RuntimeException("设置推荐商品失败：" + e.getMessage());
        }
    }

    private Map<String, Object> convertProductToMap(Product product) {
        Map<String, Object> productMap = new HashMap<>();
        productMap.put("id", product.getId());
        productMap.put("name", product.getName());
        productMap.put("description", product.getDescription());
        productMap.put("price", product.getPrice());
        productMap.put("categoryId", product.getCategoryId());
        productMap.put("merchantId", product.getMerchantId());
        productMap.put("imageUrl", product.getImageUrl());
        productMap.put("stock", product.getStock());
        productMap.put("sales", product.getSales());
        productMap.put("status", product.getStatus());
        productMap.put("statusDesc", product.getStatus() == 1 ? "上架" : "下架");
        productMap.put("isHot", product.getIsHot());
        productMap.put("rating", product.getRating());
        productMap.put("createTime", product.getCreateTime());
        productMap.put("updateTime", product.getUpdateTime());
        return productMap;
    }
}