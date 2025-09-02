package com.xuexian.jigsaw.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuexian.jigsaw.entity.Piece;
import com.xuexian.jigsaw.entity.Record;
import com.xuexian.jigsaw.vo.Result;
import org.springframework.stereotype.Service;

import java.util.List;

public interface IPieceService extends IService<Piece> {
    List<Piece> listByJigsawId(Integer jigsawId);

    Result undo(Long jigsawId, Long userId);

    Result reset(Long jigsawId, Long userId);

    Result saveOrComplete(Long jigsawId, String piecesJson);

    Result getCurrentPieces(Long jigsawId);
}
