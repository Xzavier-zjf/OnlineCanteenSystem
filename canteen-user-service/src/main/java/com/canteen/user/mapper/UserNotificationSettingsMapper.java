package com.canteen.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.canteen.user.entity.UserNotificationSettings;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户通知设置Mapper接口
 */
@Mapper
public interface UserNotificationSettingsMapper extends BaseMapper<UserNotificationSettings> {
}