package com.xuexian.jigsaw.controller.user;

import com.xuexian.jigsaw.entity.Jigsaw;
import com.xuexian.jigsaw.service.IJigsawService;
import com.xuexian.jigsaw.util.UserHolder;
import com.xuexian.jigsaw.vo.Result;
import com.xuexian.jigsaw.vo.ScrollResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static com.xuexian.jigsaw.util.Code.JIGSAW_NOT_EXIST;
import static com.xuexian.jigsaw.util.Code.REQUEST_SUCCESS;

@RestController
@RequestMapping("/jigsaw")
@Tag(name = "拼图相关接口", description = "拼图模块相关操作接口")
public class JigsawController {

    private final IJigsawService jigsawService;

    public JigsawController(IJigsawService jigsawService) {
        this.jigsawService = jigsawService;
    }

    /**
     * 拼图列表
     */
    @GetMapping("/scroll")
    @Operation(summary = "滚动查询拼图列表", description = "以创建时间为分数的滚动查询,将上一次查询返回的offset和时间作为参数请求")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功")
    })
    public Result<ScrollResult<Jigsaw>> scrollJigsaw(
            @Parameter(description = "分类ID", required = true) @RequestParam Integer categoryId,
            @Parameter(description = "最大时间戳", required = false) @RequestParam(required = false) Long maxTime,
            @Parameter(description = "分页偏移量", required = false) @RequestParam(required = false) Integer offset,
            @Parameter(description = "分页限制数量", required = false) @RequestParam(defaultValue = "10") Integer limit
    ) {
        Long userId = UserHolder.getUser().getId(); // 获取当前用户
        LocalDateTime maxDateTime = maxTime != null ?
                LocalDateTime.ofInstant(Instant.ofEpochMilli(maxTime), ZoneId.systemDefault())
                : null;
        ScrollResult<Jigsaw> result = jigsawService.scrollJigsawByCategory(categoryId, maxDateTime, offset, limit, userId);
        return Result.success(REQUEST_SUCCESS, result);
    }

    @GetMapping("/{jigsawId}/full-image")
    @Operation(summary = "获取完整拼图图片", description = "根据拼图ID返回拼图完整图片URL")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功"),
            @ApiResponse(responseCode = "-10003", description = "拼图不存在")
    })
    public Result<String> getFullImage(
            @Parameter(description = "拼图ID", required = true) @PathVariable Long jigsawId
    ) {
        Jigsaw jigsaw = jigsawService.getById(jigsawId);
        if (jigsaw == null) {
            return Result.error(JIGSAW_NOT_EXIST, "拼图不存在");
        }
        return Result.success(REQUEST_SUCCESS, jigsaw.getUrl());
    }
}
