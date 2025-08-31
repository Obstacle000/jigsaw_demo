package com.xuexian.jigsaw.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScrollResult<T> {
    private List<T> list;    // 当前页数据
    private Long minTime;    // 当前页最小时间戳
    private Integer offset;  // 当前页偏移量，用于处理同一时间戳的多条数据
}
