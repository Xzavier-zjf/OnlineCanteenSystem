package com.canteen.product.config;

import com.canteen.product.entity.Product;
import com.canteen.product.entity.ProductCategory;
import com.canteen.product.mapper.ProductMapper;
import com.canteen.product.mapper.ProductCategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 数据初始化器 - 创建测试数据
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ProductCategoryMapper categoryMapper;

    @Override
    public void run(String... args) throws Exception {
        try {
            // 检查是否已有数据
            Long categoryCount = categoryMapper.selectCount(null);
            if (categoryCount > 0) {
                System.out.println("数据已存在，跳过初始化");
                return;
            }

            System.out.println("开始初始化测试数据...");

            // 初始化分类数据
            initCategories();

            // 初始化商品数据
            initProducts();

            System.out.println("测试数据初始化完成！");
        } catch (Exception e) {
            System.err.println("数据初始化失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initCategories() {
        List<ProductCategory> categories = Arrays.asList(
            createCategory(1L, "主食套餐", "营养丰富的主食套餐"),
            createCategory(2L, "面食类", "各种面条和面食"),
            createCategory(3L, "汤品类", "营养汤品"),
            createCategory(4L, "素食类", "健康素食"),
            createCategory(5L, "荤菜类", "肉类菜品"),
            createCategory(6L, "饮品类", "各种饮料"),
            createCategory(7L, "小食点心", "小食和点心"),
            createCategory(8L, "早餐类", "早餐食品")
        );

        for (ProductCategory category : categories) {
            categoryMapper.insert(category);
        }
        System.out.println("已初始化 " + categories.size() + " 个分类");
    }

    private void initProducts() {
        List<Product> products = Arrays.asList(
            // 主食套餐
            createProduct("红烧肉套餐", "经典红烧肉配米饭", new BigDecimal("18.00"), 1L, true),
            createProduct("宫保鸡丁套餐", "宫保鸡丁配米饭", new BigDecimal("16.00"), 1L, false),
            createProduct("糖醋里脊套餐", "糖醋里脊配米饭", new BigDecimal("17.00"), 1L, true),

            // 面食类
            createProduct("兰州拉面", "正宗兰州牛肉拉面", new BigDecimal("12.00"), 2L, true),
            createProduct("炸酱面", "老北京炸酱面", new BigDecimal("11.00"), 2L, false),
            createProduct("西红柿鸡蛋面", "家常西红柿鸡蛋面", new BigDecimal("10.00"), 2L, false),

            // 汤品类
            createProduct("紫菜蛋花汤", "清淡紫菜蛋花汤", new BigDecimal("6.00"), 3L, false),
            createProduct("冬瓜排骨汤", "营养冬瓜排骨汤", new BigDecimal("8.00"), 3L, true),

            // 素食类
            createProduct("麻婆豆腐", "经典川菜麻婆豆腐", new BigDecimal("9.00"), 4L, false),
            createProduct("清炒时蔬", "新鲜时令蔬菜", new BigDecimal("7.00"), 4L, false),

            // 荤菜类
            createProduct("可乐鸡翅", "香甜可乐鸡翅", new BigDecimal("14.00"), 5L, true),
            createProduct("红烧排骨", "家常红烧排骨", new BigDecimal("16.00"), 5L, false),

            // 饮品类
            createProduct("柠檬蜂蜜茶", "清香柠檬蜂蜜茶", new BigDecimal("5.00"), 6L, false),
            createProduct("奶茶", "香浓奶茶", new BigDecimal("6.00"), 6L, true),

            // 小食点心
            createProduct("小笼包", "上海小笼包", new BigDecimal("8.00"), 7L, true),
            createProduct("煎饺", "香脆煎饺", new BigDecimal("7.00"), 7L, false),

            // 早餐类
            createProduct("豆浆油条", "经典早餐组合", new BigDecimal("4.00"), 8L, false),
            createProduct("小米粥配咸菜", "养胃小米粥", new BigDecimal("3.00"), 8L, false)
        );

        for (Product product : products) {
            productMapper.insert(product);
        }
        System.out.println("已初始化 " + products.size() + " 个商品");
    }

    private ProductCategory createCategory(Long id, String name, String description) {
        ProductCategory category = new ProductCategory();
        category.setId(id);
        category.setName(name);
        category.setDescription(description);
        category.setStatus(1);
        category.setSortOrder(id.intValue());
        category.setCreateTime(LocalDateTime.now());
        return category;
    }

    private Product createProduct(String name, String description, BigDecimal price, Long categoryId, boolean isHot) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setCategoryId(categoryId);
        product.setMerchantId(1L);
        product.setImageUrl("/images/products/" + name.replaceAll("[^a-zA-Z0-9]", "") + ".jpg");
        product.setStock(100);
        product.setSales((int)(Math.random() * 50) + 10);
        product.setStatus(1);
        product.setIsHot(isHot ? 1 : 0);
        product.setRating(4.0 + Math.random());
        // createTime 和 updateTime 由 MyBatis Plus 自动填充
        return product;
    }
}