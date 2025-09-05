package com.canteen.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.canteen.product.dto.MerchantProductDTO;
import com.canteen.product.entity.Product;
import com.canteen.product.mapper.ProductMapper;
import com.canteen.product.service.MerchantProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 商户商品服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantProductServiceImpl implements MerchantProductService {

    private final ProductMapper productMapper;

    @Override
    public Long getMerchantProductCount(Long merchantId) {
        try {
            QueryWrapper<Product> wrapper = new QueryWrapper<>();
            wrapper.eq("merchant_id", merchantId);
            return productMapper.selectCount(wrapper);
        } catch (Exception e) {
            log.error("获取商户商品总数失败：merchantId={}", merchantId, e);
            return 0L;
        }
    }

    @Override
    public List<Map<String, Object>> getTopProducts(Long merchantId, Integer limit) {
        try {
            QueryWrapper<Product> wrapper = new QueryWrapper<>();
            wrapper.eq("merchant_id", merchantId)
                   .eq("status", 1)
                   .orderByDesc("sales")
                   .orderByDesc("rating")
                   .last("LIMIT " + limit);

            List<Product> products = productMapper.selectList(wrapper);
            return products.stream().map(product -> {
                Map<String, Object> productMap = new HashMap<>();
                productMap.put("id", product.getId());
                productMap.put("name", product.getName());
                productMap.put("sales", product.getSales());
                productMap.put("revenue", product.getPrice().multiply(new BigDecimal(product.getSales())));
                return productMap;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取商户热销商品失败：merchantId={}", merchantId, e);
            return new ArrayList<>();
        }
    }

    @Override
    public Map<String, Object> getMerchantProductList(Long merchantId, Integer page, Integer size, String keyword, Integer status) {
        try {
            Page<Product> pageParam = new Page<>(page, size);
            QueryWrapper<Product> wrapper = new QueryWrapper<>();
            wrapper.eq("merchant_id", merchantId);

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
            log.error("获取商户商品列表失败：merchantId={}", merchantId, e);
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
    public void addProduct(Long merchantId, MerchantProductDTO.AddProductRequest request) {
        try {
            Product product = new Product();
            product.setName(request.getName());
            product.setDescription(request.getDescription());
            product.setPrice(request.getPrice());
            product.setCategoryId(request.getCategoryId());
            product.setMerchantId(merchantId);
            product.setImageUrl(request.getImageUrl());
            product.setStock(request.getStock());
            product.setSales(0);
            product.setStatus(0); // 默认下架状态，需要审核
            product.setIsHot(0);
            product.setRating(0.0);
            product.setCreateTime(LocalDateTime.now());
            product.setUpdateTime(LocalDateTime.now());

            productMapper.insert(product);
            log.info("商户添加商品成功：merchantId={}, productName={}", merchantId, request.getName());
        } catch (Exception e) {
            log.error("商户添加商品失败：merchantId={}", merchantId, e);
            throw new RuntimeException("添加商品失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void updateProduct(Long productId, Long merchantId, MerchantProductDTO.UpdateProductRequest request) {
        try {
            Product product = productMapper.selectById(productId);
            if (product == null || !merchantId.equals(product.getMerchantId())) {
                throw new RuntimeException("商品不存在或无权限操作");
            }

            // 更新商品信息
            if (StringUtils.hasText(request.getName())) {
                product.setName(request.getName());
            }
            if (StringUtils.hasText(request.getDescription())) {
                product.setDescription(request.getDescription());
            }
            if (request.getPrice() != null) {
                product.setPrice(request.getPrice());
            }
            if (request.getCategoryId() != null) {
                product.setCategoryId(request.getCategoryId());
            }
            if (StringUtils.hasText(request.getImageUrl())) {
                product.setImageUrl(request.getImageUrl());
            }
            if (request.getStock() != null) {
                product.setStock(request.getStock());
            }

            product.setUpdateTime(LocalDateTime.now());
            productMapper.updateById(product);

            log.info("商户更新商品成功：merchantId={}, productId={}", merchantId, productId);
        } catch (Exception e) {
            log.error("商户更新商品失败：merchantId={}, productId={}", merchantId, productId, e);
            throw new RuntimeException("更新商品失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void deleteProduct(Long productId, Long merchantId) {
        try {
            Product product = productMapper.selectById(productId);
            if (product == null || !merchantId.equals(product.getMerchantId())) {
                throw new RuntimeException("商品不存在或无权限操作");
            }

            // 软删除：设置状态为禁用
            product.setStatus(0);
            product.setUpdateTime(LocalDateTime.now());
            productMapper.updateById(product);

            log.info("商户删除商品成功：merchantId={}, productId={}", merchantId, productId);
        } catch (Exception e) {
            log.error("商户删除商品失败：merchantId={}, productId={}", merchantId, productId, e);
            throw new RuntimeException("删除商品失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void updateProductStatus(Long productId, Long merchantId, Integer status) {
        try {
            Product product = productMapper.selectById(productId);
            if (product == null || !merchantId.equals(product.getMerchantId())) {
                throw new RuntimeException("商品不存在或无权限操作");
            }

            product.setStatus(status);
            product.setUpdateTime(LocalDateTime.now());
            productMapper.updateById(product);

            log.info("商户更新商品状态成功：merchantId={}, productId={}, status={}", merchantId, productId, status);
        } catch (Exception e) {
            log.error("商户更新商品状态失败：merchantId={}, productId={}", merchantId, productId, e);
            throw new RuntimeException("更新商品状态失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void updateProductStock(Long productId, Long merchantId, Integer stock) {
        try {
            Product product = productMapper.selectById(productId);
            if (product == null || !merchantId.equals(product.getMerchantId())) {
                throw new RuntimeException("商品不存在或无权限操作");
            }

            product.setStock(stock);
            product.setUpdateTime(LocalDateTime.now());
            productMapper.updateById(product);

            log.info("商户更新商品库存成功：merchantId={}, productId={}, stock={}", merchantId, productId, stock);
        } catch (Exception e) {
            log.error("商户更新商品库存失败：merchantId={}, productId={}", merchantId, productId, e);
            throw new RuntimeException("更新商品库存失败：" + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getProductDetail(Long productId, Long merchantId) {
        try {
            Product product = productMapper.selectById(productId);
            if (product == null || !merchantId.equals(product.getMerchantId())) {
                throw new RuntimeException("商品不存在或无权限操作");
            }

            return convertProductToMap(product);
        } catch (Exception e) {
            log.error("获取商品详情失败：merchantId={}, productId={}", merchantId, productId, e);
            throw new RuntimeException("获取商品详情失败：" + e.getMessage());
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