package com.canteen.product.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.canteen.product.entity.Product;
import com.canteen.product.mapper.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 餐品服务类
 */
@Service
public class ProductService {

    @Autowired
    private ProductMapper productMapper;

    /**
     * 分页查询餐品
     */
    public Page<Product> getProductList(Long current, Long size, Long categoryId, String keyword) {
        try {
            Page<Product> page = new Page<>(current, size);
            QueryWrapper<Product> wrapper = new QueryWrapper<>();
            wrapper.eq("status", 1); // 只查询上架商品
            
            if (categoryId != null) {
                wrapper.eq("category_id", categoryId);
            }
            
            if (keyword != null && !keyword.trim().isEmpty()) {
                wrapper.like("name", keyword);
            }
            
            // 默认按创建时间排序
            wrapper.orderByDesc("create_time");
            
            return productMapper.selectPage(page, wrapper);
        } catch (Exception e) {
            // 如果查询失败，返回空页面
            return new Page<>(current, size);
        }
    }
    


    /**
     * 根据ID获取餐品
     */
    public Product getProductById(Long id) {
        return productMapper.selectById(id);
    }

    /**
     * 获取热门推荐
     */
    public List<Product> getHotProducts(Integer limit) {
        try {
            QueryWrapper<Product> wrapper = new QueryWrapper<>();
            wrapper.eq("status", 1);
            wrapper.eq("is_hot", 1); // 使用is_hot字段而不是sales排序
            wrapper.orderByDesc("create_time");
            wrapper.last("LIMIT " + limit);
            return productMapper.selectList(wrapper);
        } catch (Exception e) {
            // 如果查询失败，返回空列表而不是抛出异常
            return java.util.Collections.emptyList();
        }
    }

    /**
     * 获取系统统计数据 - 真实数据库查询
     */
    public Map<String, Object> getSystemStats() {
        try {
            // 使用自定义SQL查询获取真实统计数据
            return productMapper.getSystemStatistics();
        } catch (Exception e) {
            // 返回默认统计数据
            Map<String, Object> defaultStats = new HashMap<>();
            defaultStats.put("totalProducts", 0);
            defaultStats.put("totalCategories", 0);
            defaultStats.put("averagePrice", "0.00");
            defaultStats.put("hotProducts", 0);
            return defaultStats;
        }
    }
    
    /**
     * 获取热门推荐商品 - 真实数据库查询
     */
    public List<Product> getTopSalesProducts(Integer limit) {
        try {
            return productMapper.selectTopSalesProducts(limit);
        } catch (Exception e) {
            return java.util.Collections.emptyList();
        }
    }
    
    /**
     * 获取推荐商品
     */
    public List<Product> getRecommendedProducts(Long userId, Integer limit) {
        try {
            return productMapper.selectRecommendedProducts(userId, limit);
        } catch (Exception e) {
            return java.util.Collections.emptyList();
        }
    }
    
    /**
     * 获取分类统计信息
     */
    public Map<String, Object> getCategoryStatistics() {
        try {
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("categoryStats", productMapper.getCategoryStatistics());
            statistics.put("salesByCategory", productMapper.getSalesByCategory());
            return statistics;
        } catch (Exception e) {
            Map<String, Object> defaultStats = new HashMap<>();
            defaultStats.put("categoryStats", java.util.Collections.emptyList());
            defaultStats.put("salesByCategory", java.util.Collections.emptyList());
            return defaultStats;
        }
    }

    /**
     * 根据价格区间和排序方式查询餐品
     */
    public Page<Product> getProductListWithFilters(Long current, Long size, Long categoryId, String keyword, String priceRange, String sortBy) {
        try {
            Page<Product> page = new Page<>(current, size);
            QueryWrapper<Product> wrapper = new QueryWrapper<>();
            wrapper.eq("status", 1); // 只查询上架商品
            
            if (categoryId != null) {
                wrapper.eq("category_id", categoryId);
            }
            
            if (keyword != null && !keyword.trim().isEmpty()) {
                wrapper.like("name", keyword);
            }
            
            // 价格区间过滤
            if (priceRange != null && !priceRange.trim().isEmpty()) {
                switch (priceRange) {
                    case "0-10":
                        wrapper.between("price", 0, 10);
                        break;
                    case "10-20":
                        wrapper.between("price", 10, 20);
                        break;
                    case "20-50":
                        wrapper.between("price", 20, 50);
                        break;
                    case "50+":
                        wrapper.ge("price", 50);
                        break;
                }
            }
            
            // 排序方式
            if (sortBy != null && !sortBy.trim().isEmpty()) {
                switch (sortBy) {
                    case "price_asc":
                        wrapper.orderByAsc("price");
                        break;
                    case "price_desc":
                        wrapper.orderByDesc("price");
                        break;
                    case "sales_desc":
                        wrapper.orderByDesc("sales");
                        break;
                    default:
                        wrapper.orderByDesc("create_time");
                        break;
                }
            } else {
                wrapper.orderByDesc("create_time");
            }
            
            return productMapper.selectPage(page, wrapper);
        } catch (Exception e) {
            // 如果查询失败，返回空页面
            return new Page<>(current, size);
        }
    }
}