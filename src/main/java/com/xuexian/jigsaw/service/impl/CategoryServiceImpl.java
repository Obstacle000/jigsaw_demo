package com.xuexian.jigsaw.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuexian.jigsaw.entity.Category;
import com.xuexian.jigsaw.mapper.CategoryMapper;
import com.xuexian.jigsaw.service.ICategoryService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements ICategoryService {
    @Autowired
    private CategoryMapper categoryMapper;
    @Override
    public List<Category> listCategoriesWithProgress(Long userId) {
        return categoryMapper.getCategoriesWithProgress(userId);
    }
}
