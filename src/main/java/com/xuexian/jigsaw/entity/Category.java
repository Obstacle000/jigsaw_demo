package com.xuexian.jigsaw.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("category")
public class Category {
    private Integer categoryId;     // 分类ID
    private String name;            // 分类名称
    private String cover;           // 封面图URL
    private Integer total;          // 分类下拼图总数
    @TableField(exist = false)
    private Integer finishedCount;  // 用户已完成拼图数
    @TableField(exist = false)
    private Double progress;        // 用户完成度百分比
}
