package com.xuexian.jigsaw.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuexian.jigsaw.entity.User;
import com.xuexian.jigsaw.mapper.RoleMapper;
import com.xuexian.jigsaw.mapper.UserMapper;
import com.xuexian.jigsaw.mapper.UserRoleMapper;
import com.xuexian.jigsaw.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    @Autowired
    private RoleMapper roleMapper;
    @Autowired
    private UserRoleMapper userRoleMapper;

    @Override
    public List<String> getUserRoles(Long userId) {
        return roleMapper.selectRolesByUserId(userId);
    }

    @Override
    public void assignRole(Long userId, String roleName) {

        Integer roleId = roleMapper.selectRoleIdByName(roleName);
        userRoleMapper.insertUserRole(userId, roleId);
    }
}
