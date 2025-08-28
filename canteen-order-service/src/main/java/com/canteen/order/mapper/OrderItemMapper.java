package com.canteen.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.canteen.order.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单详情数据访问层
 */
@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {
}