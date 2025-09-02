package com.xuexian.jigsaw.controller.user;

import com.xuexian.jigsaw.service.IPieceService;
import com.xuexian.jigsaw.util.UserHolder;
import com.xuexian.jigsaw.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/piece")
@Tag(name = "拼图块相关接口", description = "包括保存/完成拼图，获取当前拼图状态等")
public class PieceController {

    @Autowired
    private IPieceService pieceService;

    /*
    // 如果需要撤销接口，可放开
    @PostMapping("/{jigsawId}/undo")
    @Operation(summary = "拼图撤销", description = "撤销用户上一次操作")
    public Result undo(@Parameter(description = "拼图ID", required = true) @PathVariable Long jigsawId) {
        Long userId = UserHolder.getUser().getId();
        return pieceService.undo(jigsawId, userId);
    }

    @PostMapping("/{jigsawId}/reset")
    @Operation(summary = "拼图重做", description = "重置当前拼图到初始状态")
    public Result reset(@Parameter(description = "拼图ID", required = true) @PathVariable Long jigsawId) {
        Long userId = UserHolder.getUser().getId();
        return pieceService.reset(jigsawId, userId);
    }
    */

    @PostMapping("/{jigsawId}/saveOrComplete")
    @Operation(summary = "保存或完成拼图", description = "保存当前拼图状态或标记完成")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功")
    })
    public Result saveOrComplete(
            @Parameter(description = "拼图ID", required = true) @PathVariable Long jigsawId,
            @Parameter(description = "当前拼图块状态 JSON", required = true) @RequestBody String piecesJson) {
        Long userId = UserHolder.getUser().getId();
        return pieceService.saveOrComplete(jigsawId, piecesJson);
    }

    @GetMapping("/current")
    @Operation(summary = "获取当前拼图状态", description = "返回用户保存的当前拼图状态及进度")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功")
    })
    public Result getCurrentPieces(
            @Parameter(description = "拼图ID", required = true) @RequestParam Long jigsawId) {
        Long userId = UserHolder.getUser().getId();
        return pieceService.getCurrentPieces(jigsawId);
    }

}
