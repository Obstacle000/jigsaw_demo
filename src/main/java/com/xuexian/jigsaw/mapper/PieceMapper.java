package com.xuexian.jigsaw.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuexian.jigsaw.entity.Piece;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PieceMapper extends BaseMapper<Piece> {

    @Select("SELECT * FROM piece WHERE jigsaw_id = #{jigsawId} AND deleted_at IS NULL ORDER BY piece_number")
    List<Piece> listByJigsawId(Integer jigsawId);
}
