package com.xuexian.jigsaw.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuexian.jigsaw.entity.User;
import com.xuexian.jigsaw.mapper.UserMapper;
import com.xuexian.jigsaw.service.IUserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
}
