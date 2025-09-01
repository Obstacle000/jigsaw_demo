package com.xuexian.jigsaw.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuexian.jigsaw.entity.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface IUserService  extends IService<User> {
    List<String> getUserRoles(Long userId);

    void assignRole(Long id, String user);
}
