package com.canteen.product.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.canteen.common.result.PageResult;
import com.canteen.common.result.Result;
import com.canteen.product.entity.Product;
import com.canteen.product.entity.ProductCategory;
import com.canteen.product.service.ProductService;
import com.canteen.product.service.ProductCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 餐品控制器
 */
@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductCategoryService categoryService;

    /**
     * 获取餐品列表（分页）
     */
    @GetMapping
    public Result<PageResult<Product>> getProductList(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword) {
        
        Page<Product> result = productService.getProductList(current, size, categoryId, keyword);
        PageResult<Product> pageResult = PageResult.of(
            result.getTotal(), 
            result.getRecords(), 
            result.getCurrent(), 
            result.getSize()
        );
        
        return Result.success(pageResult);
    }

    /**
     * 获取餐品详情
     */
    @GetMapping("/{id}")
    public Result<Product> getProduct(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        if (product == null) {
            return Result.error("餐品不存在");
        }
        return Result.success(product);
    }

    /**
     * 获取分类列表
     */
    @GetMapping("/categories")
    public Result<List<ProductCategory>> getCategories() {
        List<ProductCategory> categories = categoryService.getCategories();
        return Result.success(categories);
    }

    /**
     * 按分类获取产品
     */
    @GetMapping("/category/{categoryId}")
    public Result<List<Product>> getProductsByCategory(@PathVariable Long categoryId) {
        Page<Product> result = productService.getProductList(1L, 100L, categoryId, null);
        return Result.success(result.getRecords());
    }

    /**
     * 获取热门推荐
     */
    @GetMapping("/hot")
    public Result<List<Product>> getHotProducts(@RequestParam(defaultValue = "5") Integer limit) {
        List<Product> products = productService.getHotProducts(limit);
        return Result.success(products);
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("餐品服务运行正常");
    }

    /**
     * 获取系统统计数据
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getSystemStats() {
        try {
            Map<String, Object> stats = productService.getSystemStats();
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("获取统计数据失败: " + e.getMessage());
        }
    }

    /**
     * 简单测试接口
     */
    @GetMapping("/test")
    public Result<String> test() {
        try {
            return Result.success("产品服务测试成功");
        } catch (Exception e) {
            return Result.error("测试失败: " + e.getMessage());
        }
    }

}