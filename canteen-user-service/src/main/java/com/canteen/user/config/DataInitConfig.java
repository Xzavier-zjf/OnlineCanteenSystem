package com.canteen.user.config;

import com.canteen.user.entity.User;
import com.canteen.user.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 数据初始化配置
 * 在应用启动时检查并初始化基础数据
 */
@Slf4j
@Component
public class DataInitConfig implements ApplicationRunner {

    @Autowired
    private UserMapper userMapper;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("开始检查用户数据...");
        
        // 检查是否有用户数据
        List<User> users = userMapper.selectList(null);
        if (users.isEmpty()) {
            log.warn("未发现用户数据，请确保已执行数据库初始化脚本 database/real_test_data.sql");
        } else {
            log.info("发现 {} 个用户数据", users.size());
            // 显示用户统计信息
            long adminCount = users.stream().filter(u -> "ADMIN".equals(u.getRole())).count();
            long merchantCount = users.stream().filter(u -> "MERCHANT".equals(u.getRole())).count();
            long userCount = users.stream().filter(u -> "USER".equals(u.getRole())).count();
            
            log.info("用户统计 - 管理员: {}, 商户: {}, 普通用户: {}", adminCount, merchantCount, userCount);
        }
    }
}