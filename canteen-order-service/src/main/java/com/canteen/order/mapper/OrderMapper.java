package com.canteen.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.canteen.order.entity.Order;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单数据访问层
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}