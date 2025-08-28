package com.canteen.product.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * ApplicationRunner配置类
 * 解决MyBatis-Plus的ddlApplicationRunner问题
 */
@Configuration
public class ApplicationRunnerConfig {
    
    /**
     * 创建一个Primary的ddlApplicationRunner来覆盖MyBatis-Plus的NullBean
     */
    @Bean("ddlApplicationRunner")
    @Primary
    public ApplicationRunner ddlApplicationRunner() {
        return args -> {
            // 空实现，防止MyBatis-Plus的DDL runner执行
            System.out.println("Product Service DDL Application Runner executed successfully");
        };
    }
}