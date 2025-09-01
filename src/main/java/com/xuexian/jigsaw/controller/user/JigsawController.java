package com.xuexian.jigsaw.controller.user;

import com.xuexian.jigsaw.entity.Jigsaw;
import com.xuexian.jigsaw.service.IJigsawService;
import com.xuexian.jigsaw.util.UserHolder;
import com.xuexian.jigsaw.vo.Result;
import com.xuexian.jigsaw.vo.ScrollResult;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@RestController
@RequestMapping("/jigsaw")
public class JigsawController {

    private final IJigsawService jigsawService;

    public JigsawController(IJigsawService jigsawService) {
        this.jigsawService = jigsawService;
    }

    /**
     * 拼图列表
     * @param categoryId
     * @param maxTime
     * @param offset
     * @param limit
     * @return
     */
    @GetMapping("/scroll")
    public Result<ScrollResult<Jigsaw>> scrollJigsaw(@RequestParam Integer categoryId,
                                                     @RequestParam(required = false) Long maxTime,
                                                     @RequestParam(required = false) Integer offset,
                                                     @RequestParam(defaultValue = "10") Integer limit) {
        Long userId = UserHolder.getUser().getId(); // 获取当前用户
        LocalDateTime maxDateTime = maxTime != null ?
                LocalDateTime.ofInstant(Instant.ofEpochMilli(maxTime), ZoneId.systemDefault())
                : null;
        ScrollResult<Jigsaw> result = jigsawService.scrollJigsawByCategory(categoryId, maxDateTime, offset, limit, userId);
        return Result.success(result);
    }

    @GetMapping("/{jigsawId}/full-image")
    public Result<String> getFullImage(@PathVariable Long jigsawId) {
        Jigsaw jigsaw = jigsawService.getById(jigsawId);
        if (jigsaw == null) {
            return Result.error("拼图不存在");
        }
        return Result.success(jigsaw.getUrl()); // 返回完整图片 URL
    }


}
