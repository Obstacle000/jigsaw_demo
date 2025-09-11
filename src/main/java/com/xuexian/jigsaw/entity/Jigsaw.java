package com.xuexian.jigsaw.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("jigsaw")
@Schema(description = "拼图实体，包含拼图的基本信息")
public class Jigsaw {

    @TableId(type = IdType.AUTO)
    @Schema(description = "拼图ID", example = "1001")
    private Long id;

    @Schema(description = "所属分类ID", example = "1")
    private Integer categoryId;

    @Schema(description = "拼图标题", example = "蒙娜丽莎的微笑")
    private String title;

    @Schema(description = "拼图背景介绍", example = "1999年...")
    private String background;

    @Schema(description = "拼图块数量", example = "48")
    private Integer pieceCount;

    @Schema(description = "拼图创建时间", example = "2025-09-02T15:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "拼图文件URL", example = "https://example.com/jigsaw.zip")
    private String url;

    @Schema(description = "用户是否完成", example = "true")
    @TableField(exist = false)
    private boolean done;
}
