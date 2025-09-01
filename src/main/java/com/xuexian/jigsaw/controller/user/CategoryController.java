package com.xuexian.jigsaw.controller.user;

import com.xuexian.jigsaw.entity.Category;
import com.xuexian.jigsaw.service.ICategoryService;
import com.xuexian.jigsaw.vo.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final ICategoryService categoryService;

    public CategoryController(ICategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * 返回分类列表
     * @param userId
     * @return
     */
    @GetMapping
    public Result<List<Category>> listCategories(@RequestParam Long userId) {
        List<Category> categories = categoryService.listCategoriesWithProgress(userId);
        return Result.success(categories);
    }
}