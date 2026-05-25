package com.canteen.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.canteen.user.entity.UserLoginRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户登录记录Mapper接口
 */
@Mapper
public interface UserLoginRecordMapper extends BaseMapper<UserLoginRecord> {
}