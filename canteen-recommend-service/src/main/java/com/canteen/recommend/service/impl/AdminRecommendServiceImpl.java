package com.canteen.recommend.service.impl;

import com.canteen.recommend.service.AdminRecommendService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
    private final Map<String, Object> recommendConfig = new ConcurrentHashMap<>(defaultRecommendConfig());

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

    @Override
    public Map<String, Object> getRecommendConfig() {
        return new HashMap<>(recommendConfig);
    }

    @Override
    public Map<String, Object> saveRecommendConfig(Map<String, Object> config) {
        Map<String, Object> normalizedConfig = normalizeRecommendConfig(config);
        recommendConfig.clear();
        recommendConfig.putAll(normalizedConfig);
        log.info("推荐策略配置保存成功：{}", normalizedConfig);
        return new HashMap<>(recommendConfig);
    }

    @Override
    public List<Map<String, Object>> getTrendData(Integer days, String type) {
        int safeDays = days == null ? 7 : Math.max(1, Math.min(days, 30));
        List<Map<String, Object>> trend = new ArrayList<>();
        for (int i = safeDays - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            Map<String, Object> item = new HashMap<>();
            item.put("date", date.toString());
            if ("hot".equals(type)) {
                item.put("views", 120 + (safeDays - i) * 8 + hotProductIds.size() * 3);
                item.put("orders", 18 + (safeDays - i) * 2 + hotProductIds.size());
            } else {
                item.put("clickRate", 10.0 + (safeDays - i) * 0.8 + recommendProductIds.size() * 0.15);
                item.put("conversionRate", 5.0 + (safeDays - i) * 0.4 + recommendProductIds.size() * 0.08);
            }
            trend.add(item);
        }
        return trend;
    }

    private static Map<String, Object> defaultRecommendConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("algorithm", "comprehensive");
        config.put("salesWeight", 0.4);
        config.put("ratingWeight", 0.3);
        config.put("timeWeight", 0.3);
        config.put("updateFrequency", "daily");
        config.put("maxRecommendCount", 20);
        return config;
    }

    private Map<String, Object> normalizeRecommendConfig(Map<String, Object> config) {
        Map<String, Object> normalized = defaultRecommendConfig();
        if (config == null) {
            return normalized;
        }

        normalized.put("algorithm", String.valueOf(config.getOrDefault("algorithm", normalized.get("algorithm"))));
        normalized.put("updateFrequency", String.valueOf(config.getOrDefault("updateFrequency", normalized.get("updateFrequency"))));
        normalized.put("salesWeight", clampDouble(config.get("salesWeight"), 0, 1, 0.4));
        normalized.put("ratingWeight", clampDouble(config.get("ratingWeight"), 0, 1, 0.3));
        normalized.put("timeWeight", clampDouble(config.get("timeWeight"), 0, 1, 0.3));
        normalized.put("maxRecommendCount", clampInt(config.get("maxRecommendCount"), 5, 50, 20));
        return normalized;
    }

    private double clampDouble(Object value, double min, double max, double fallback) {
        try {
            double parsed = Double.parseDouble(String.valueOf(value));
            return Math.max(min, Math.min(max, parsed));
        } catch (Exception e) {
            return fallback;
        }
    }

    private int clampInt(Object value, int min, int max, int fallback) {
        try {
            int parsed = Integer.parseInt(String.valueOf(value));
            return Math.max(min, Math.min(max, parsed));
        } catch (Exception e) {
            return fallback;
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
