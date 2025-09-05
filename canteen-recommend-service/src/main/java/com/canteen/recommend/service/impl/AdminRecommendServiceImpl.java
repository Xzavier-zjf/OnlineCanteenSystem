package com.canteen.recommend.service.impl;

import com.canteen.recommend.service.AdminRecommendService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * 管理员推荐服务实现
 */
@Slf4j
@Service
public class AdminRecommendServiceImpl implements AdminRecommendService {

    @Value("${canteen.product-service.url:http://localhost:8082}")
    private String productServiceUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    // 模拟推荐商品存储（实际应该存储在数据库中）
    private final Set<Long> recommendProductIds = new HashSet<>();
    private final Set<Long> hotProductIds = new HashSet<>();

    @Override
    public List<Map<String, Object>> getRecommendProducts() {
        try {
            List<Map<String, Object>> allProducts = getAllProducts();
            return allProducts.stream()
                    .filter(product -> recommendProductIds.contains(((Number) product.get("id")).longValue()))
                    .collect(java.util.stream.Collectors.toList());
        } catch (Exception e) {
            log.error("获取推荐商品列表失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public void setRecommendProducts(List<Long> productIds) {
        try {
            recommendProductIds.clear();
            recommendProductIds.addAll(productIds);
            log.info("设置推荐商品成功：{}", productIds);
        } catch (Exception e) {
            log.error("设置推荐商品失败", e);
            throw new RuntimeException("设置推荐商品失败：" + e.getMessage());
        }
    }

    @Override
    public List<Map<String, Object>> getHotProducts() {
        try {
            List<Map<String, Object>> allProducts = getAllProducts();
            return allProducts.stream()
                    .filter(product -> hotProductIds.contains(((Number) product.get("id")).longValue()))
                    .collect(java.util.stream.Collectors.toList());
        } catch (Exception e) {
            log.error("获取热销商品列表失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public void setHotProducts(List<Long> productIds) {
        try {
            hotProductIds.clear();
            hotProductIds.addAll(productIds);
            log.info("设置热销商品成功：{}", productIds);
        } catch (Exception e) {
            log.error("设置热销商品失败", e);
            throw new RuntimeException("设置热销商品失败：" + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getRecommendStatistics() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // 推荐商品数量
            stats.put("recommendCount", recommendProductIds.size());
            
            // 热销商品数量
            stats.put("hotCount", hotProductIds.size());
            
            // 总商品数量
            List<Map<String, Object>> allProducts = getAllProducts();
            stats.put("totalProducts", allProducts.size());
            
            // 推荐覆盖率
            double recommendCoverage = allProducts.isEmpty() ? 0.0 : 
                    (double) recommendProductIds.size() / allProducts.size() * 100;
            stats.put("recommendCoverage", Math.round(recommendCoverage * 100.0) / 100.0);
            
            return stats;
        } catch (Exception e) {
            log.error("获取推荐统计信息失败", e);
            return new HashMap<>();
        }
    }

    private List<Map<String, Object>> getAllProducts() {
        try {
            String url = productServiceUrl + "/api/products?current=1&size=1000";
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response != null && "200".equals(String.valueOf(response.get("code")))) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) response.get("data");
                
                if (data != null) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> products = (List<Map<String, Object>>) data.get("records");
                    return products != null ? products : new ArrayList<>();
                }
            }
            
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("获取所有商品失败", e);
            return new ArrayList<>();
        }
    }
}