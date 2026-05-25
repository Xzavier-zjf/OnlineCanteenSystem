package com.canteen.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.canteen.user.entity.MerchantSettings;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商户设置Mapper接口
 */
@Mapper
public interface MerchantSettingsMapper extends BaseMapper<MerchantSettings> {
}