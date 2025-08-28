package com.canteen.recommend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

/**
 * 推荐服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendService {

    private final WebClient.Builder webClientBuilder;

    /**
     * 获取热门推荐
     */
    public List<Map<String, Object>> getHotRecommend(Integer limit) {
        try {
            // 暂时不调用产品服务，直接使用模拟数据
            log.info("获取热门推荐成功，数量: {}", limit);
            return createMockHotProducts(limit);
        } catch (Exception e) {
            log.error("获取热门推荐失败", e);
            // 降级处理：返回默认推荐
            return createMockHotProducts(limit);
        }
    }

    /**
     * 获取个性化推荐
     */
    public List<Map<String, Object>> getPersonalRecommend(Long userId, Integer limit) {
        try {
            log.info("为用户 {} 获取个性化推荐", userId);
            
            // 简化的个性化推荐算法
            // 1. 获取用户历史订单偏好
            List<String> userPreferences = getUserPreferences(userId);
            
            // 2. 基于偏好推荐相似餐品
            List<Map<String, Object>> recommendations = generatePersonalizedRecommendations(userPreferences, limit);
            
            // 3. 如果推荐数量不足，补充热门推荐
            if (recommendations.size() < limit) {
                List<Map<String, Object>> hotProducts = getHotRecommend(limit - recommendations.size());
                recommendations.addAll(hotProducts);
            }
            
            return recommendations.subList(0, Math.min(recommendations.size(), limit));
            
        } catch (Exception e) {
            log.error("获取个性化推荐失败", e);
            // 降级处理：返回热门推荐
            return getHotRecommend(limit);
        }
    }

    /**
     * 获取新品推荐
     */
    public List<Map<String, Object>> getNewRecommend(Integer limit) {
        try {
            // 暂时不调用产品服务，直接使用模拟数据
            log.info("获取新品推荐，数量: {}", limit);
            return createMockNewProducts(limit);
        } catch (Exception e) {
            log.error("获取新品推荐失败", e);
            return createMockNewProducts(limit);
        }
    }

    /**
     * 获取相似产品推荐
     */
    public List<Map<String, Object>> getSimilarProducts(Long productId, Integer limit) {
        try {
            log.info("获取产品 {} 的相似推荐", productId);
            
            // 简化的相似产品推荐算法
            // 1. 根据产品ID获取产品信息（这里简化处理）
            String category = getProductCategory(productId);
            
            // 2. 基于分类推荐相似产品
            List<Map<String, Object>> similarProducts = getProductsByCategory(category, limit + 1);
            
            // 3. 移除当前产品本身
            similarProducts.removeIf(product -> productId.equals(product.get("id")));
            
            // 4. 如果相似产品不足，补充热门推荐
            if (similarProducts.size() < limit) {
                List<Map<String, Object>> hotProducts = getHotRecommend(limit - similarProducts.size());
                similarProducts.addAll(hotProducts);
            }
            
            return similarProducts.subList(0, Math.min(similarProducts.size(), limit));
            
        } catch (Exception e) {
            log.error("获取相似产品推荐失败", e);
            // 降级处理：返回热门推荐
            return getHotRecommend(limit);
        }
    }

    /**
     * 根据产品ID获取产品分类（简化实现）
     */
    private String getProductCategory(Long productId) {
        // 这里应该调用产品服务获取真实的产品信息
        // 暂时根据ID模拟分类
        if (productId <= 2) return "主食";
        if (productId <= 4) return "汤类";
        if (productId <= 6) return "饮品";
        if (productId <= 8) return "荤菜";
        return "素菜";
    }

    /**
     * 获取用户偏好（简化实现）
     */
    private List<String> getUserPreferences(Long userId) {
        // 这里应该分析用户的历史订单数据
        // 暂时返回模拟偏好
        List<String> preferences = new ArrayList<>();
        
        // 根据用户ID模拟不同偏好
        if (userId % 3 == 0) {
            preferences.addAll(Arrays.asList("荤菜", "主食"));
        } else if (userId % 3 == 1) {
            preferences.addAll(Arrays.asList("素菜", "汤类"));
        } else {
            preferences.addAll(Arrays.asList("小食", "饮品"));
        }
        
        return preferences;
    }

    /**
     * 基于偏好生成个性化推荐
     */
    private List<Map<String, Object>> generatePersonalizedRecommendations(List<String> preferences, Integer limit) {
        List<Map<String, Object>> recommendations = new ArrayList<>();
        
        // 根据偏好生成推荐（简化实现）
        for (String preference : preferences) {
            recommendations.addAll(getProductsByCategory(preference, limit / preferences.size()));
        }
        
        return recommendations;
    }

    /**
     * 根据分类获取餐品（模拟实现）
     */
    private List<Map<String, Object>> getProductsByCategory(String category, Integer limit) {
        List<Map<String, Object>> products = new ArrayList<>();
        
        // 模拟不同分类的餐品
        switch (category) {
            case "荤菜":
                products.add(createMockProduct(1L, "红烧肉盖饭", "15.00", "荤菜"));
                products.add(createMockProduct(8L, "宫保鸡丁", "16.00", "荤菜"));
                break;
            case "素菜":
                products.add(createMockProduct(4L, "麻婆豆腐", "10.00", "素菜"));
                break;
            case "汤类":
                products.add(createMockProduct(3L, "紫菜蛋花汤", "8.00", "汤类"));
                break;
            case "小食":
                products.add(createMockProduct(7L, "小笼包", "8.00", "小食"));
                break;
            case "饮品":
                products.add(createMockProduct(6L, "鲜橙汁", "6.00", "饮品"));
                break;
            default:
                products.add(createMockProduct(2L, "西红柿鸡蛋面", "12.00", "主食"));
        }
        
        return products.subList(0, Math.min(products.size(), limit));
    }

    /**
     * 创建模拟热门餐品
     */
    private List<Map<String, Object>> createMockHotProducts(Integer limit) {
        List<Map<String, Object>> products = new ArrayList<>();
        products.add(createMockProduct(1L, "红烧肉盖饭", "15.00", "荤菜", 120));
        products.add(createMockProduct(3L, "紫菜蛋花汤", "8.00", "汤类", 200));
        products.add(createMockProduct(6L, "鲜橙汁", "6.00", "饮品", 150));
        products.add(createMockProduct(7L, "小笼包", "8.00", "小食", 110));
        products.add(createMockProduct(4L, "麻婆豆腐", "10.00", "素菜", 95));
        
        return products.subList(0, Math.min(products.size(), limit));
    }

    /**
     * 创建模拟新品
     */
    private List<Map<String, Object>> createMockNewProducts(Integer limit) {
        List<Map<String, Object>> products = new ArrayList<>();
        products.add(createMockProduct(9L, "蒸蛋羹", "7.00", "汤类"));
        products.add(createMockProduct(10L, "酸辣土豆丝", "9.00", "素菜"));
        products.add(createMockProduct(11L, "可乐", "4.00", "饮品"));
        products.add(createMockProduct(12L, "煎饺", "10.00", "小食"));
        
        return products.subList(0, Math.min(products.size(), limit));
    }

    /**
     * 创建模拟餐品数据
     */
    private Map<String, Object> createMockProduct(Long id, String name, String price, String category) {
        return createMockProduct(id, name, price, category, 0);
    }

    private Map<String, Object> createMockProduct(Long id, String name, String price, String category, Integer sales) {
        Map<String, Object> product = new HashMap<>();
        product.put("id", id);
        product.put("name", name);
        product.put("price", price);
        product.put("category", category);
        product.put("sales", sales);
        product.put("imageUrl", "/images/" + name.toLowerCase().replaceAll("[^a-z0-9]", "") + ".jpg");
        return product;
    }
}