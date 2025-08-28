package com.canteen.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * 用户服务启动类
 */
@SpringBootApplication
@EnableDiscoveryClient  // 启用Nacos服务发现
@ComponentScan(basePackages = {"com.canteen.user", "com.canteen.common"})
@MapperScan("com.canteen.user.mapper")
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
        System.out.println("=================================");
        System.out.println("用户服务启动成功！");
        System.out.println("端口: 8081");
        System.out.println("=================================");
    }
}