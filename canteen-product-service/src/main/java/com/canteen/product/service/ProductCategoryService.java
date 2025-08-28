package com.canteen.product.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.canteen.product.entity.ProductCategory;
import com.canteen.product.mapper.ProductCategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 餐品分类服务类
 */
@Service
public class ProductCategoryService {

    @Autowired
    private ProductCategoryMapper categoryMapper;

    /**
     * 获取分类列表
     */
    public List<ProductCategory> getCategories() {
        QueryWrapper<ProductCategory> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1);
        wrapper.orderByAsc("sort_order");
        return categoryMapper.selectList(wrapper);
    }
}