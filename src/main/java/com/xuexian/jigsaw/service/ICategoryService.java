package com.xuexian.jigsaw.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuexian.jigsaw.entity.Category;
import org.springframework.stereotype.Service;

import java.util.List;

public interface ICategoryService extends IService<Category> {
    List<Category> listCategoriesWithProgress(Long userId);
}
