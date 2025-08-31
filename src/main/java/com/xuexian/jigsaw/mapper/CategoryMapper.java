package com.xuexian.jigsaw.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuexian.jigsaw.entity.Category;
import io.lettuce.core.dynamic.annotation.Param;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {

    @Select("SELECT " +
            "c.category_id, c.name, c.cover, COUNT(j.id) AS total, " +
            "IFNULL(r.finished_count, 0) AS finished_count, " +
            "ROUND(IFNULL(r.finished_count, 0)/COUNT(j.id)*100, 2) AS progress " +
            "FROM category c " +
            "LEFT JOIN jigsaw j ON j.category_id = c.category_id AND j.deleted_at IS NULL " +
            "LEFT JOIN ( " +
            "   SELECT j.category_id, COUNT(*) AS finished_count " +
            "   FROM record r " +
            "   JOIN jigsaw j ON r.jigsaw_id = j.id " +
            "   WHERE r.user_id = #{userId} " +
            "   GROUP BY j.category_id " +
            ") r ON r.category_id = c.category_id " +
            "WHERE c.deleted_at IS NULL " +
            "GROUP BY c.category_id, c.name, c.cover, r.finished_count " +
            "ORDER BY c.created_at ASC")
    List<Category> getCategoriesWithProgress(@Param("userId") Long userId);
}

