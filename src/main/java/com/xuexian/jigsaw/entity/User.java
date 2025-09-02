package com.xuexian.jigsaw.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user")
@Schema(description = "用户实体，包含用户基本信息与认证相关字段")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "用户ID（主键）", example = "1001")
    private Long id;

    @Schema(description = "用户名", example = "zhangsan")
    private String userName;

    @Schema(description = "密码（加密存储）", example = "******")
    private String password;

    @Schema(description = "昵称（默认随机生成）", example = "user_10000")
    private String nickName;

    @Schema(description = "用户头像URL", example = "https://example.com/avatar.png")
    private String icon = "";

    @Schema(description = "账户创建时间", example = "2025-09-02T16:20:00")
    private LocalDateTime createTime;

    @Schema(description = "账户更新时间", example = "2025-09-02T16:40:00")
    private LocalDateTime updateTime;

    @Schema(description = "用户等级", example = "3")
    private Long level = 1L;

    @TableLogic
    @Schema(description = "删除时间（为空表示未删除）")
    private LocalDateTime deletedAt;

    @TableField(exist = false)
    @Schema(description = "用户角色列表")
    private List<String> roles;
}
