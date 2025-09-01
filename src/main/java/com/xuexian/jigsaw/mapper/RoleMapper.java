package com.xuexian.jigsaw.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RoleMapper {
    @Select("SELECT r.role_name FROM role r " +
            "INNER JOIN user_role ur ON r.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId}")
    List<String> selectRolesByUserId(Long userId);

    @Select("SELECT role_id FROM role WHERE role_name = #{roleName}")
    Integer selectRoleIdByName(String roleName);


}
