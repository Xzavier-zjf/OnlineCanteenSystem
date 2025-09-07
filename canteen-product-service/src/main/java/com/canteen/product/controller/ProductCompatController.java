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

/**
 * 产品兼容性控制器 - 处理前端的 /api/product 路径请求
 */
@RestController
@RequestMapping("/api/product")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class ProductCompatController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductCategoryService categoryService;

    /**
     * 兼容前端的 /api/product/products 请求
     */
    @GetMapping("/products")
    public Result<PageResult<Product>> getProducts(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String priceRange,
            @RequestParam(required = false) String sortBy) {
        
        try {
            System.out.println("收到商品列表请求: current=" + current + ", size=" + size + 
                             ", categoryId=" + categoryId + ", sortBy=" + sortBy);
            
            Page<Product> result;
            
            // 如果有高级筛选参数，使用带筛选的方法
            if (priceRange != null || sortBy != null) {
                result = productService.getProductListWithFilters(current, size, categoryId, keyword, priceRange, sortBy);
            } else {
                result = productService.getProductList(current, size, categoryId, keyword);
            }
            
            PageResult<Product> pageResult = PageResult.of(
                result.getTotal(), 
                result.getRecords(), 
                result.getCurrent(), 
                result.getSize()
            );
            
            System.out.println("返回商品数据: 总数=" + result.getTotal() + ", 当前页=" + result.getRecords().size());
            
            return Result.success(pageResult);
        } catch (Exception e) {
            System.err.println("获取商品列表失败: " + e.getMessage());
            e.printStackTrace();
            return Result.error("获取商品列表失败: " + e.getMessage());
        }
    }

    /**
     * 兼容前端的 /api/product/products/categories 请求
     */
    @GetMapping("/products/categories")
    public Result<List<ProductCategory>> getProductCategories() {
        try {
            System.out.println("收到商品分类请求");
            List<ProductCategory> categories = categoryService.getCategories();
            System.out.println("返回分类数据: " + categories.size() + " 个分类");
            return Result.success(categories);
        } catch (Exception e) {
            System.err.println("获取商品分类失败: " + e.getMessage());
            e.printStackTrace();
            return Result.error("获取商品分类失败: " + e.getMessage());
        }
    }

    /**
     * 兼容前端的 /api/product/products/{id} 请求
     */
    @GetMapping("/products/{id}")
    public Result<Product> getProductById(@PathVariable Long id) {
        try {
            System.out.println("收到商品详情请求: id=" + id);
            Product product = productService.getProductById(id);
            if (product == null) {
                return Result.error("餐品不存在");
            }
            return Result.success(product);
        } catch (Exception e) {
            System.err.println("获取商品详情失败: " + e.getMessage());
            e.printStackTrace();
            return Result.error("获取商品详情失败: " + e.getMessage());
        }
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("产品兼容服务运行正常");
    }
}