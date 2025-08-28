package com.canteen.user.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * 应用启动器配置
 * 用于解决MyBatis-Plus DDL Runner冲突问题
 */
@Configuration
public class ApplicationRunnerConfig {

    /**
     * 创建一个空的ApplicationRunner来替代有问题的ddlApplicationRunner
     */
    @Bean("ddlApplicationRunner")
    @Primary
    public ApplicationRunner ddlApplicationRunner() {
        return args -> {
            // 空实现，不执行任何DDL操作
        };
    }
}