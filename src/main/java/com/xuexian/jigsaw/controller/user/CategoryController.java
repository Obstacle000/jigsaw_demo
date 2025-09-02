package com.xuexian.jigsaw.controller.user;

import com.xuexian.jigsaw.entity.Category;
import com.xuexian.jigsaw.service.ICategoryService;
import com.xuexian.jigsaw.util.UserHolder;
import com.xuexian.jigsaw.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.xuexian.jigsaw.util.Code.REQUEST_SUCCESS;
import static com.xuexian.jigsaw.util.Code.SYSTEM_UNKNOWN_ERR;

@RestController
@RequestMapping("/categories")
@Tag(name = "拼图分类相关接口", description = "提供拼图分类列表相关操作")
public class CategoryController {

    private final ICategoryService categoryService;

    public CategoryController(ICategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * 返回分类列表
     */
    @GetMapping
    @Operation(summary = "返回分类列表", description = "获取当前用户的所有拼图分类及完成进度")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功")
    })
    public Result<List<Category>> listCategories() {
        Long userId = UserHolder.getUser().getId();
        List<Category> categories = categoryService.listCategoriesWithProgress(userId);
        if (categories == null) {
            return Result.error(SYSTEM_UNKNOWN_ERR, "渲染失败");
        }
        return Result.success(REQUEST_SUCCESS, categories);
    }
}
