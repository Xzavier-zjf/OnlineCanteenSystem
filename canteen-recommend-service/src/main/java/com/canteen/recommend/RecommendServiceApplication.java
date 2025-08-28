package com.canteen.recommend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * 推荐服务启动类
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.canteen.recommend", "com.canteen.common"})
public class RecommendServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RecommendServiceApplication.class, args);
        System.out.println("=================================");
        System.out.println("推荐服务启动成功！");
        System.out.println("端口: 8084");
        System.out.println("=================================");
    }
}