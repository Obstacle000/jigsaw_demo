package com.xuexian.jigsaw;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xuexian.jigsaw.entity.Piece;
import com.xuexian.jigsaw.mapper.PieceMapper;
import com.xuexian.jigsaw.util.PasswordEncoder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class JigsawApplicationTests {
    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    private PieceMapper pieceMapper;
    @Test
    void test() {

        List<Piece> pieces = pieceMapper.selectList(new QueryWrapper<Piece>().eq("jigsaw_id", 67));
        System.out.println(pieces);
    }

}
