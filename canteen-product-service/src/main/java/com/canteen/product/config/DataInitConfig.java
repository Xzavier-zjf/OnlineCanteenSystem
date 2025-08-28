package com.canteen.product.config;

import com.canteen.product.entity.Product;
import com.canteen.product.mapper.ProductMapper;
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
    private ProductMapper productMapper;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("开始检查产品数据...");
        
        // 检查是否有产品数据
        List<Product> products = productMapper.selectList(null);
        if (products.isEmpty()) {
            log.warn("未发现产品数据，请确保已执行数据库初始化脚本 database/real_test_data.sql");
        } else {
            log.info("发现 {} 个产品数据", products.size());
            // 显示前几个产品作为示例
            products.stream().limit(5).forEach(product -> 
                log.info("产品: {} - 价格: {} - 库存: {}", 
                    product.getName(), product.getPrice(), product.getStock())
            );
        }
    }
}