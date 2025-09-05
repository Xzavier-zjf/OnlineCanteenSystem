package com.canteen.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.canteen.order.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 订单数据访问层
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {
    
    /**
     * 根据用户ID查询订单
     */
    List<Order> selectByUserId(Long userId);
    
    /**
     * 根据商户ID查询订单（通过订单项中的商品关联）
     */
    @Select("SELECT DISTINCT o.* FROM `order` o " +
            "INNER JOIN order_item oi ON o.id = oi.order_id " +
            "INNER JOIN product p ON oi.product_id = p.id " +
            "WHERE p.merchant_id = #{merchantId}")
    List<Order> selectByMerchantId(Long merchantId);
    
    /**
     * 查询商户的待处理订单
     */
    @Select("SELECT DISTINCT o.* FROM `order` o " +
            "INNER JOIN order_item oi ON o.id = oi.order_id " +
            "INNER JOIN product p ON oi.product_id = p.id " +
            "WHERE p.merchant_id = #{merchantId} AND o.status IN ('PENDING', 'PAID')")
    List<Order> selectPendingOrdersByMerchantId(Long merchantId);
    
    /**
     * 统计商户待处理订单数量
     */
    @Select("SELECT COUNT(DISTINCT o.id) FROM `order` o " +
            "INNER JOIN order_item oi ON o.id = oi.order_id " +
            "INNER JOIN product p ON oi.product_id = p.id " +
            "WHERE p.merchant_id = #{merchantId} AND o.status IN ('PENDING', 'PAID')")
    int countPendingOrdersByMerchantId(Long merchantId);
    
    /**
     * 分页查询所有订单（管理员用）
     */
    @Select("SELECT * FROM `order` ORDER BY create_time DESC LIMIT #{limit} OFFSET #{offset}")
    List<Order> selectAllOrdersWithPagination(@Param("offset") int offset, @Param("limit") int limit);
    
    /**
     * 统计所有订单数量
     */
    @Select("SELECT COUNT(*) FROM `order`")
    int countAllOrders();
    
    /**
     * 按状态统计订单数量
     */
    @Select("SELECT COUNT(*) FROM `order` WHERE status = #{status}")
    int countOrdersByStatus(String status);
    
    /**
     * 按时间范围统计订单数据
     */
    @Select("SELECT DATE(create_time) as date, COUNT(*) as orderCount, SUM(total_amount) as totalAmount " +
            "FROM `order` WHERE create_time BETWEEN #{startDate} AND #{endDate} " +
            "GROUP BY DATE(create_time) ORDER BY date")
    List<Map<String, Object>> getOrderStatsByDateRange(@Param("startDate") String startDate, @Param("endDate") String endDate);
}