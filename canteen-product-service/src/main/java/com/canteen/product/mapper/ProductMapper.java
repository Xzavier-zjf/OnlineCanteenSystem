package com.canteen.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.canteen.product.entity.Product;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 餐品Mapper接口 - 真实数据库查询
 * 移除所有模拟数据，实现真实的数据库操作
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {
    
    /**
     * 查询热门商品
     */
    @Select("SELECT * FROM product WHERE status = 1 AND is_hot = 1 ORDER BY sales DESC, rating DESC LIMIT #{limit}")
    List<Product> selectHotProducts(int limit);
    
    /**
     * 查询销量前N的商品
     */
    @Select("SELECT * FROM product WHERE status = 1 ORDER BY sales DESC LIMIT #{limit}")
    List<Product> selectTopSalesProducts(int limit);
    
    /**
     * 获取平均评分
     */
    @Select("SELECT ROUND(AVG(rating), 2) FROM product WHERE status = 1 AND rating > 0")
    BigDecimal getAverageRating();
    
    /**
     * 获取总销量
     */
    @Select("SELECT SUM(sales) FROM product WHERE status = 1")
    Integer getTotalSales();
    
    /**
     * 基于用户历史订单推荐商品
     */
    @Select("SELECT DISTINCT p.* FROM product p " +
            "JOIN order_item oi ON p.id = oi.product_id " +
            "JOIN orders o ON oi.order_id = o.id " +
            "WHERE o.user_id = #{userId} AND p.status = 1 " +
            "ORDER BY p.sales DESC, p.rating DESC LIMIT #{limit}")
    List<Product> selectRecommendedProducts(@Param("userId") Long userId, @Param("limit") int limit);
    
    /**
     * 获取分类统计信息
     */
    @Select("SELECT pc.name as categoryName, COUNT(p.id) as productCount, " +
            "AVG(p.rating) as avgRating, SUM(p.sales) as totalSales " +
            "FROM product_category pc " +
            "LEFT JOIN product p ON pc.id = p.category_id AND p.status = 1 " +
            "WHERE pc.status = 1 " +
            "GROUP BY pc.id, pc.name " +
            "ORDER BY totalSales DESC")
    List<Map<String, Object>> getCategoryStatistics();
    
    /**
     * 获取各分类销量统计
     */
    @Select("SELECT pc.name as categoryName, SUM(p.sales) as sales " +
            "FROM product_category pc " +
            "LEFT JOIN product p ON pc.id = p.category_id AND p.status = 1 " +
            "WHERE pc.status = 1 " +
            "GROUP BY pc.id, pc.name " +
            "ORDER BY sales DESC")
    List<Map<String, Object>> getSalesByCategory();
    
    /**
     * 更新商品销量
     */
    @Update("UPDATE product SET sales = sales + #{quantity} WHERE id = #{productId}")
    int updateSales(@Param("productId") Long productId, @Param("quantity") int quantity);
    
    /**
     * 更新商品库存
     */
    @Update("UPDATE product SET stock = stock - #{quantity} WHERE id = #{productId} AND stock >= #{quantity}")
    int updateStock(@Param("productId") Long productId, @Param("quantity") int quantity);
    
    /**
     * 获取系统统计数据
     */
    @Select("SELECT " +
            "COUNT(*) as totalProducts, " +
            "COUNT(DISTINCT category_id) as totalCategories, " +
            "ROUND(AVG(price), 2) as averagePrice, " +
            "SUM(CASE WHEN is_hot = 1 THEN 1 ELSE 0 END) as hotProducts " +
            "FROM product WHERE status = 1")
    Map<String, Object> getSystemStatistics();
}