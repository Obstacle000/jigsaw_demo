package com.xuexian.jigsaw.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuexian.jigsaw.entity.Piece;
import com.xuexian.jigsaw.exception.BusinessException;
import com.xuexian.jigsaw.mapper.PieceMapper;
import com.xuexian.jigsaw.service.IPieceService;
import com.xuexian.jigsaw.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.xuexian.jigsaw.entity.Code.JIGSAW_UNDO_FAIL;
import static com.xuexian.jigsaw.util.RedisConstants.CURRENT_KEY;
import static com.xuexian.jigsaw.util.RedisConstants.HISTORY_KEY;

@Service
public class PieceServiceImpl extends ServiceImpl<PieceMapper, Piece> implements IPieceService {

    @Autowired
    private PieceMapper pieceMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public List<Piece> listByJigsawId(Integer jigsawId) {
        return pieceMapper.listByJigsawId(jigsawId);
    }

    /**
     * 拼图撤销
     * @param jigsawId
     * @param userId
     * @return
     */
    @Override
    public Result undo(Long jigsawId, Long userId) {
        String historyKey = String.format(HISTORY_KEY, jigsawId, userId);
        String currentKey = String.format(CURRENT_KEY, jigsawId, userId);

        // 从历史栈取出上一步状态
        String lastState = stringRedisTemplate.opsForList().rightPop(historyKey);
        if (lastState == null) {
            throw new BusinessException(JIGSAW_UNDO_FAIL, "已经是初始状态，无法撤销");
        }

        // 更新当前状态
        stringRedisTemplate.opsForValue().set(currentKey, lastState);

        // 解析完成度
        List<Map<String, Object>> pieces = JSONUtil.toBean(lastState, List.class);
        long total = pieces.size();
        long placedCount = pieces.stream().filter(p -> Boolean.TRUE.equals(p.get("placed"))).count();
        int progress = (int)(placedCount * 100 / total);

        Map<String, Object> response = new HashMap<>();
        response.put("pieces", pieces);
        response.put("progress", progress);

        return Result.success(response);
    }

    /**
     * 拼图重来
     * @param jigsawId
     * @param userId
     * @return
     */
    @Override
    public Result reset(Long jigsawId, Long userId) {
        String currentKey = String.format(CURRENT_KEY, jigsawId, userId);
        String historyKey = String.format(HISTORY_KEY, jigsawId, userId);

        // 删除当前拼图状态
        stringRedisTemplate.delete(currentKey);

        // 清空历史栈
        stringRedisTemplate.delete(historyKey);

        // 返回初始拼图状态
        // 假设初始状态从数据库获取
        String initialStateJson = getInitialJigsawState(jigsawId);
        stringRedisTemplate.opsForValue().set(currentKey, initialStateJson);

        List<Map> pieces = JSONUtil.toBean(initialStateJson, List.class);

        Map<String, Object> response = new HashMap<>();
        response.put("pieces", pieces);
        response.put("progress", 0); // 重来后进度为0%

        return Result.success(response);
    }

    private String getInitialJigsawState(Long jigsawId) {
        // 查询数据库获取该拼图的所有拼图块
        List<Piece> pieces = listByJigsawId(jigsawId.intValue());

        List<Map<String, Object>> initialPieces = new ArrayList<>();
        int startX = 10; // 待拖动区起始 X 坐标
        int startY = 500; // 待拖动区 Y 坐标
        int gap = 100; // 每个拼图块水平间隔

        for (int i = 0; i < pieces.size(); i++) {
            Piece p = pieces.get(i);
            Map<String, Object> map = new HashMap<>();
            map.put("pieceNumber", p.getPieceNumber());
            map.put("x", startX + i * gap); // 每个拼图块水平错开
            map.put("y", startY); // 同一行
            map.put("placed", false);
            map.put("url", p.getUrl());
            initialPieces.add(map);
        }

        return JSONUtil.toJsonStr(initialPieces);
    }
}
