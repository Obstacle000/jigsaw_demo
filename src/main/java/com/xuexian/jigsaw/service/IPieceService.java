package com.xuexian.jigsaw.service;

import com.xuexian.jigsaw.entity.Piece;
import com.xuexian.jigsaw.vo.Result;

import java.util.List;

public interface IPieceService {
    List<Piece> listByJigsawId(Integer jigsawId);

    Result undo(Long jigsawId, Long userId);

    Result reset(Long jigsawId, Long userId);
}
