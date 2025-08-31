package com.xuexian.jigsaw.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("jigsaw")
public class Jigsaw {
    private Long id;
    private Integer categoryId;
    private String title;
    private String background;
    private Integer pieceCount;
    private LocalDateTime createdAt;
}