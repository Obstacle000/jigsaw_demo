package com.xuexian.jigsaw.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuexian.jigsaw.entity.Jigsaw;
import io.lettuce.core.dynamic.annotation.Param;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface JigsawMapper extends BaseMapper<Jigsaw> {

    @Select("SELECT * FROM jigsaw " +
            "WHERE category_id = #{categoryId} AND deleted_at IS NULL " +
            "AND created_at <= #{maxTime} " +
            "ORDER BY created_at DESC " +
            "LIMIT #{limit} OFFSET #{offset}")
    List<Jigsaw> scrollByCategory(@Param("categoryId") Integer categoryId,
                                  @Param("maxTime") LocalDateTime maxTime,
                                  @Param("offset") Integer offset,
                                  @Param("limit") Integer limit);
}