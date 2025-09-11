package com.xuexian.jigsaw.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("category")
@Schema(description = "分类实体，包含分类基础信息和用户进度统计")
public class Category {

    @TableId(type = IdType.AUTO)
    @Schema(description = "分类ID", example = "1")
    private Integer id;     // 分类ID

    @Schema(description = "分类名称", example = "世界名画拼图")
    private String name;            // 分类名称

    @Schema(description = "封面图URL", example = "https://example.com/cover.png")
    private String cover;           // 封面图URL

    @Schema(description = "分类下拼图总数", example = "20")
    private Integer total;          // 分类下拼图总数

    @TableField(exist = false)
    @Schema(description = "用户已完成拼图数", example = "5")
    private Integer finishedCount;  // 用户已完成拼图数

    @Schema(description = "分类创建时间", example = "2025-09-02T15:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "分类删除时间", example = "2025-09-02T15:30:00")
    private LocalDateTime deletedAt;

    @Schema(description = "分类更新时间", example = "2025-09-02T15:30:00")
    private LocalDateTime updatedAt;


    @TableField(exist = false)
    @Schema(description = "用户完成度百分比", example = "0.25")
    private Double progress;        // 用户完成度百分比
}
