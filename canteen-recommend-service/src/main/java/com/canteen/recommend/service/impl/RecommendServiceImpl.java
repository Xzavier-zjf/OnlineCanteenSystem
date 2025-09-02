package com.canteen.recommend.service.impl;

import com.canteen.recommend.entity.RecommendProduct;
import com.canteen.recommend.entity.UserBehavior;
import com.canteen.recommend.service.RecommendService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 推荐服务实现类
 */
@Slf4j
@Service
public class RecommendServiceImpl implements RecommendService {

    @Value("${canteen.product-service.url:http://localhost:8082}")
    private String productServiceUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public List<RecommendProduct> getPersonalizedRecommendations(Long userId, Integer limit) {
        try {
            // 调用产品服务获取热门商品作为个性化推荐的基础
            String url = productServiceUrl + "/api/products/hot?limit=" + limit;
            log.info("调用产品服务获取热门商品: {}", url);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            List<RecommendProduct> recommendations = new ArrayList<>();
            
            if (response != null && "200".equals(String.valueOf(response.get("code")))) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> products = (List<Map<String, Object>>) response.get("data");
                
                if (products != null) {
                    for (Map<String, Object> product : products) {
                        RecommendProduct recommend = convertToRecommendProduct(product);
                        recommend.setReason("基于您的浏览历史推荐");
                        recommend.setScore(0.8 + Math.random() * 0.2); // 模拟推荐分数
                        recommendations.add(recommend);
                    }
                }
            }
            
            return recommendations;
        } catch (Exception e) {
            log.error("获取个性化推荐失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<RecommendProduct> getHotRecommendations(Integer limit) {
        try {
            // 调用产品服务获取热门商品
            String url = productServiceUrl + "/api/products/hot?limit=" + limit;
            log.info("调用产品服务获取热门商品: {}", url);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            List<RecommendProduct> recommendations = new ArrayList<>();
            
            if (response != null && "200".equals(String.valueOf(response.get("code")))) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> products = (List<Map<String, Object>>) response.get("data");
                
                if (products != null) {
                    for (Map<String, Object> product : products) {
                        RecommendProduct recommend = convertToRecommendProduct(product);
                        recommend.setReason("热门商品");
                        recommend.setScore(0.9 + Math.random() * 0.1); // 热门商品分数较高
                        recommendations.add(recommend);
                    }
                }
            }
            
            return recommendations;
        } catch (Exception e) {
            log.error("获取热门推荐失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<RecommendProduct> getSimilarProducts(Long productId, Integer limit) {
        try {
            // 调用产品服务获取商品详情，然后根据分类推荐相似商品
            String detailUrl = productServiceUrl + "/api/products/" + productId;
            @SuppressWarnings("unchecked")
            Map<String, Object> detailResponse = restTemplate.getForObject(detailUrl, Map.class);
            
            if (detailResponse != null && "200".equals(String.valueOf(detailResponse.get("code")))) {
                @SuppressWarnings("unchecked")
                Map<String, Object> product = (Map<String, Object>) detailResponse.get("data");
                
                if (product != null && product.get("categoryId") != null) {
                    Long categoryId = Long.valueOf(String.valueOf(product.get("categoryId")));
                    
                    // 获取同分类的其他商品
                    String categoryUrl = productServiceUrl + "/api/products/category/" + categoryId;
                    @SuppressWarnings("unchecked")
                    Map<String, Object> categoryResponse = restTemplate.getForObject(categoryUrl, Map.class);
                    
                    List<RecommendProduct> recommendations = new ArrayList<>();
                    
                    if (categoryResponse != null && "200".equals(String.valueOf(categoryResponse.get("code")))) {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> products = (List<Map<String, Object>>) categoryResponse.get("data");
                        
                        if (products != null) {
                            int count = 0;
                            for (Map<String, Object> similarProduct : products) {
                                // 排除当前商品
                                if (!productId.equals(Long.valueOf(String.valueOf(similarProduct.get("id"))))) {
                                    RecommendProduct recommend = convertToRecommendProduct(similarProduct);
                                    recommend.setReason("相似商品推荐");
                                    recommend.setScore(0.7 + Math.random() * 0.2);
                                    recommendations.add(recommend);
                                    
                                    if (++count >= limit) {
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    
                    return recommendations;
                }
            }
            
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("获取相似商品推荐失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public boolean recordUserBehavior(UserBehavior behavior) {
        try {
            // 这里可以将用户行为记录到数据库或消息队列
            // 用于后续的推荐算法优化
            log.info("记录用户行为: userId={}, productId={}, action={}", 
                    behavior.getUserId(), behavior.getProductId(), behavior.getAction());
            return true;
        } catch (Exception e) {
            log.error("记录用户行为失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 将产品数据转换为推荐商品
     */
    private RecommendProduct convertToRecommendProduct(Map<String, Object> product) {
        RecommendProduct recommend = new RecommendProduct();
        recommend.setId(Long.valueOf(String.valueOf(product.get("id"))));
        recommend.setName(String.valueOf(product.get("name")));
        recommend.setDescription(String.valueOf(product.get("description")));
        
        // 处理价格
        Object priceObj = product.get("price");
        if (priceObj != null) {
            recommend.setPrice(new BigDecimal(String.valueOf(priceObj)));
        }
        
        recommend.setImageUrl(String.valueOf(product.get("imageUrl")));
        
        // 处理销量
        Object salesObj = product.get("sales");
        if (salesObj != null) {
            recommend.setSales(Integer.valueOf(String.valueOf(salesObj)));
        }
        
        // 处理评分
        Object ratingObj = product.get("rating");
        if (ratingObj != null) {
            recommend.setRating(Double.valueOf(String.valueOf(ratingObj)));
        } else {
            recommend.setRating(4.0 + Math.random()); // 默认评分
        }
        
        return recommend;
    }

    @Override
    public List<Map<String, Object>> getHotRecommend(Integer limit) {
        try {
            List<RecommendProduct> products = getHotRecommendations(limit);
            return products.stream().map(this::convertToMap).collect(java.util.stream.Collectors.toList());
        } catch (Exception e) {
            log.error("获取热门推荐失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<Map<String, Object>> getPersonalRecommend(Long userId, Integer limit) {
        try {
            List<RecommendProduct> products = getPersonalizedRecommendations(userId, limit);
            return products.stream().map(this::convertToMap).collect(java.util.stream.Collectors.toList());
        } catch (Exception e) {
            log.error("获取个性化推荐失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<Map<String, Object>> getNewRecommend(Integer limit) {
        try {
            // 获取最新的商品作为新品推荐
            String url = productServiceUrl + "/api/products?current=1&size=" + limit;
            log.info("调用产品服务获取新品: {}", url);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            List<Map<String, Object>> recommendations = new ArrayList<>();
            
            if (response != null && "200".equals(String.valueOf(response.get("code")))) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) response.get("data");
                
                if (data != null) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> products = (List<Map<String, Object>>) data.get("records");
                    
                    if (products != null) {
                        for (Map<String, Object> product : products) {
                            Map<String, Object> recommend = new java.util.HashMap<>();
                            recommend.put("id", product.get("id"));
                            recommend.put("name", product.get("name"));
                            recommend.put("price", product.get("price"));
                            recommend.put("imageUrl", product.get("imageUrl"));
                            recommend.put("description", product.get("description"));
                            recommend.put("reason", "新品推荐");
                            recommend.put("score", 0.8 + Math.random() * 0.2);
                            recommendations.add(recommend);
                        }
                    }
                }
            }
            
            return recommendations;
        } catch (Exception e) {
            log.error("获取新品推荐失败", e);
            return new ArrayList<>();
        }
    }

    private Map<String, Object> convertToMap(RecommendProduct product) {
        Map<String, Object> map = new java.util.HashMap<>();
        map.put("id", product.getId());
        map.put("name", product.getName());
        map.put("price", product.getPrice());
        map.put("imageUrl", product.getImageUrl());
        map.put("description", product.getDescription());
        map.put("sales", product.getSales());
        map.put("rating", product.getRating());
        map.put("reason", product.getReason());
        map.put("score", product.getScore());
        return map;
    }
}