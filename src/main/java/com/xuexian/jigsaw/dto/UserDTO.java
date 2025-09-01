package com.xuexian.jigsaw.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String nickName;
    private List<String> roles;

    /** 判断是否管理员 */
    public boolean isAdmin() {
        return roles != null && roles.contains("ADMIN");
    }

    /** 安全获取角色列表，避免 roles 为 null */
    public List<String> getRoles() {
        return roles != null ? roles : Collections.emptyList();
    }
}
