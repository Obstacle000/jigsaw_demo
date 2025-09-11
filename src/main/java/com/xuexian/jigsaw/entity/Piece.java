package com.xuexian.jigsaw.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("piece")
@Schema(description = "拼图块实体，包含拼图块的基础信息")
public class Piece {

    @TableId(type = IdType.AUTO)
    @Schema(description = "拼图块ID（主键）", example = "10001")
    private Integer id;

    @Schema(description = "拼图块图片地址", example = "https://example.com/piece_1.png")
    private String url;

    @Schema(description = "拼图块编号（用于排序/定位）", example = "1")
    private Integer pieceNumber;

    @Schema(description = "所属拼图ID", example = "1001")
    private Integer jigsawId;

    @Schema(description = "创建时间", example = "2025-09-02T15:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间", example = "2025-09-02T15:40:00")
    private LocalDateTime updatedAt;


    @Schema(description = "删除时间（为空表示未删除）", example = "null")
    private LocalDateTime deletedAt;
}
