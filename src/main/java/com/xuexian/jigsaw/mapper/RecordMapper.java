package com.xuexian.jigsaw.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuexian.jigsaw.entity.Record;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RecordMapper extends BaseMapper<Record> {
    @Select("""
    SELECT COUNT(1) > 0
    FROM record
    WHERE user_id = #{userId} AND jigsaw_id = #{jigsawId}
""")
    boolean  isUserDone(Long userId, Long jigsawId);

}
