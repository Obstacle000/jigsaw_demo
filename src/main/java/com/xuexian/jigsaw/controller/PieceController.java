package com.xuexian.jigsaw.controller;

import com.xuexian.jigsaw.service.IJigsawService;
import com.xuexian.jigsaw.service.IPieceService;
import com.xuexian.jigsaw.util.UserHolder;
import com.xuexian.jigsaw.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/piece")
public class PieceController {
    @Autowired
    private IPieceService pieceService;

    /**
     * 拼图撤销接口
     * @param jigsawId
     * @return
     */
    @PostMapping("/{jigsawId}/undo")
    public Result undo(@PathVariable Long jigsawId) {
        Long userId = UserHolder.getUser().getId();
        return pieceService.undo(jigsawId, userId);
    }

    /**
     * 拼图重来接口
     * @param jigsawId
     * @return
     */
    @PostMapping("/{jigsawId}/reset")
    public Result reset(@PathVariable Long jigsawId) {
        Long userId = UserHolder.getUser().getId();
        return pieceService.reset(jigsawId, userId);
    }
}
