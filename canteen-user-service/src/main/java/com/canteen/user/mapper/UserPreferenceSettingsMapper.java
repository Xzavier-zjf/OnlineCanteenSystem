package com.canteen.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.canteen.user.entity.UserPreferenceSettings;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户偏好设置Mapper接口
 */
@Mapper
public interface UserPreferenceSettingsMapper extends BaseMapper<UserPreferenceSettings> {
}