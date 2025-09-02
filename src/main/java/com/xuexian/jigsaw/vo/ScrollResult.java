package com.xuexian.jigsaw.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "通用滚动分页结果，用于返回分页数据、时间戳和偏移量")
public class ScrollResult<T> {

    @Schema(description = "当前页数据列表")
    private List<T> list;

    @Schema(description = "当前页的最小时间戳，用于下一次分页查询", example = "1725264000000")
    private Long minTime;

    @Schema(description = "当前页偏移量，用于处理同一时间戳的多条数据", example = "5")
    private Integer offset;
}
