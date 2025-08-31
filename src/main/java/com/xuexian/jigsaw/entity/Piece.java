package com.xuexian.jigsaw.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("piece")
public class Piece {

    @TableId
    private Integer id;            // 主键

    private String url;            // 拼图块图片地址

    private Integer pieceNumber;   // 拼图块编号

    private Integer jigsawId;      // 所属拼图ID

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt; // 可为空
}
