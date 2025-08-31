package com.xuexian.jigsaw.controller;

import cn.hutool.json.JSONUtil;
import com.xuexian.jigsaw.service.IJigsawService;
import com.xuexian.jigsaw.service.IPieceService;
import com.xuexian.jigsaw.util.UserHolder;
import com.xuexian.jigsaw.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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

    /**
     * 拼图保存或完成接口
     * @param jigsawId 拼图ID
     * @param piecesJson 当前拼图块状态JSON
     */
    @PostMapping("/{jigsawId}/saveOrComplete")
    public Result saveOrComplete(@PathVariable Long jigsawId, @RequestBody String piecesJson) {
        Long userId = UserHolder.getUser().getId();
        return pieceService.saveOrComplete(jigsawId, userId, piecesJson);
    }

    /**
     * 保存或完成拼图接口
     */
    @PostMapping("/{jigsawId}/saveOrComplete")
    public Result saveOrComplete(@PathVariable Long jigsawId,
                                 @RequestBody Map<String, Object> body) {
        Long userId = UserHolder.getUser().getId();
        String piecesJson = JSONUtil.toJsonStr(body.get("pieces"));
        return pieceService.saveOrComplete(jigsawId, userId, piecesJson);
    }


}
