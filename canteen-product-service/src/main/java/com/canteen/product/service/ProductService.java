package com.canteen.product.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.canteen.product.entity.Product;
import com.canteen.product.mapper.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 餐品服务类
 */
@Service
public class ProductService {

    @Autowired
    private ProductMapper productMapper;

    /**
     * 分页查询餐品
     */
    public Page<Product> getProductList(Long current, Long size, Long categoryId, String keyword) {
        try {
            Page<Product> page = new Page<>(current, size);
            QueryWrapper<Product> wrapper = new QueryWrapper<>();
            wrapper.eq("status", 1); // 只查询上架商品
            
            if (categoryId != null) {
                wrapper.eq("category_id", categoryId);
            }
            
            if (keyword != null && !keyword.trim().isEmpty()) {
                wrapper.like("name", keyword);
            }
            
            wrapper.orderByDesc("create_time");
            
            return productMapper.selectPage(page, wrapper);
        } catch (Exception e) {
            // 如果查询失败，返回空页面
            return new Page<>(current, size);
        }
    }

    /**
     * 根据ID获取餐品
     */
    public Product getProductById(Long id) {
        return productMapper.selectById(id);
    }

    /**
     * 获取热门推荐
     */
    public List<Product> getHotProducts(Integer limit) {
        try {
            QueryWrapper<Product> wrapper = new QueryWrapper<>();
            wrapper.eq("status", 1);
            wrapper.eq("is_hot", 1); // 使用is_hot字段而不是sales排序
            wrapper.orderByDesc("create_time");
            wrapper.last("LIMIT " + limit);
            return productMapper.selectList(wrapper);
        } catch (Exception e) {
            // 如果查询失败，返回空列表而不是抛出异常
            return java.util.Collections.emptyList();
        }
    }
}