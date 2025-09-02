package com.xuexian.jigsaw.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@TableName("record")
@Schema(description = "拼图完成记录实体，记录用户完成拼图的情况")
public class Record {

    @TableId(type = IdType.AUTO)
    @Schema(description = "记录ID（主键）", example = "50001")
    private Integer id;

    @Schema(description = "用户ID", example = "1001")
    private Integer userId;

    @Schema(description = "拼图ID", example = "2001")
    private Integer jigsawId;

    @Schema(description = "记录创建时间", example = "2025-09-02T16:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "记录更新时间", example = "2025-09-02T16:30:00")
    private LocalDateTime updatedAt;

    @TableLogic
    @Schema(description = "记录删除时间（为空表示未删除）", example = "null")
    private LocalDateTime deletedAt;
}
