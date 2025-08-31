package com.xuexian.jigsaw.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuexian.jigsaw.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
