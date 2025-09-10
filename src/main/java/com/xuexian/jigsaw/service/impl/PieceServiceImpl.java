package com.xuexian.jigsaw.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuexian.jigsaw.entity.Jigsaw;
import com.xuexian.jigsaw.entity.Piece;
import com.xuexian.jigsaw.entity.Record;
import com.xuexian.jigsaw.exception.BusinessException;
import com.xuexian.jigsaw.mapper.JigsawMapper;
import com.xuexian.jigsaw.mapper.PieceMapper;
import com.xuexian.jigsaw.mapper.RecordMapper;
import com.xuexian.jigsaw.service.IPieceService;
import com.xuexian.jigsaw.util.UserHolder;
import com.xuexian.jigsaw.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.*;

import static com.xuexian.jigsaw.util.Code.JIGSAW_UNDO_FAIL;
import static com.xuexian.jigsaw.util.Code.REQUEST_SUCCESS;
import static com.xuexian.jigsaw.util.RedisConstants.*;

@Service
public class PieceServiceImpl extends ServiceImpl<PieceMapper, Piece> implements IPieceService {

    @Autowired
    private PieceMapper pieceMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private JigsawMapper jigsawMapper;

    @Autowired
    private RecordMapper recordMapper;

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
    /*@Override
    public Result undo(Long jigsawId, Long userId) {
        String historyKey = String.format(HISTORY_KEY, jigsawId, userId);
        String currentKey = String.format(CURRENT_KEY, jigsawId, userId);

        // 从历史栈取出上一步状态
        String lastState = stringRedisTemplate.opsForList().rightPop(historyKey);
        if (lastState == null) {
            return Result.error(JIGSAW_UNDO_FAIL,"拼图撤销失败");
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

        return Result.success(REQUEST_SUCCESS,response);
    }*/

    /**
     * 拼图重来
     * @param jigsawId
     * @return
     */
    /*@Override
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

        return Result.success(REQUEST_SUCCESS,response);
    }*/

    // 从数据库获取去模板json
    // 从数据库获取拼图模板 JSON（随机位置）
    // 从数据库获取拼图模板 JSON（正确位置+打乱顺序）
    private String getInitialJigsawState(Long jigsawId) {
        // 查询数据库获取该拼图的所有拼图块
        List<Piece> pieces = listByJigsawId(jigsawId.intValue());

        List<Map<String, Object>> initialPieces = new ArrayList<>();

        // 假设 rows/cols 根据拼图块数量自动推算
        int pieceCount = pieces.size();
        int rows = (int) Math.sqrt(pieceCount);
        int cols = (int) Math.ceil((double) pieceCount / rows);

        // 根据拼图块序号推算出每块的正确行列坐标
        for (Piece p : pieces) {
            int pieceNumber = p.getPieceNumber();
            // 假设 pieceNumber 从 1 开始
            int correctRow = (pieceNumber - 1) / cols + 1; // 行
            int correctCol = (pieceNumber - 1) % cols + 1; // 列

            Map<String, Object> map = new HashMap<>();
            map.put("pieceNumber", pieceNumber);
            map.put("x", 0); // 初始未拼上，统一 (0,0)
            map.put("y", 0);
            map.put("correctX", correctRow); // 表格坐标
            map.put("correctY", correctCol);
            map.put("placed", false);
            map.put("url", p.getUrl());
            initialPieces.add(map);
        }

        // 打乱顺序
        Collections.shuffle(initialPieces);

        return JSONUtil.toJsonStr(initialPieces);
    }



    @Override
    public Result saveOrComplete(Long jigsawId, String piecesJson) {
        Long userId = UserHolder.getUser().getId();
        String currentKey = String.format(CURRENT_KEY, jigsawId, userId);
        String historyKey = String.format(HISTORY_KEY, jigsawId, userId);

        // 解析拼图块状态
        List<Map<String, Object>> pieces = JSONUtil.toBean(piecesJson, List.class);

        long total = pieces.size();
        long placedCount = pieces.stream().filter(p -> Boolean.TRUE.equals(p.get("placed"))).count();

        // 判断是否全部正确（可加 isInCorrectPosition 逻辑）
        boolean completed = placedCount == total;

        // 保存当前状态到 Redis
        stringRedisTemplate.opsForValue().set(currentKey, piecesJson);



        Map<String, Object> response = new HashMap<>();
        response.put("pieces", pieces);

        if (!completed) {
            int progress = (int) (placedCount * 100 / total);
            response.put("progress", progress);
            response.put("completed", false);
        } else {
            // 拼图完成，记录完成到 record 表
            Record record = new Record();
            record.setUserId(userId.intValue());
            record.setJigsawId(jigsawId.intValue());
            record.setCreatedAt(LocalDateTime.now());
            record.setUpdatedAt(LocalDateTime.now());
            recordMapper.insert(record);

            response.put("progress", 100);
            response.put("completed", true);
            response.put("background", getJigsawBackground(jigsawId));
        }

        return Result.success(REQUEST_SUCCESS,response);
    }


    /**
     * 查询拼图背景故事
     */
    private String getJigsawBackground(Long jigsawId) {
        Jigsaw jigsaw = jigsawMapper.selectById(jigsawId);
        return jigsaw != null ? jigsaw.getBackground() : "";
    }

    @Override
    public Result getCurrentPieces(Long jigsawId) {
        Long id = UserHolder.getUser().getId();
        String userKey = String.format(CURRENT_KEY, jigsawId, id);

        // 尝试从 Redis 获取用户状态
        String currentStateJson = stringRedisTemplate.opsForValue().get(userKey);
        if (currentStateJson == null) {
            // 用户第一次玩，从模板复制
            String initKey = String.format(INITIAL_KEY, jigsawId, 0L);
            currentStateJson = stringRedisTemplate.opsForValue().get(initKey);
            if (currentStateJson == null) {
                // 模板不存在，从数据库生成
                currentStateJson = getInitialJigsawState(jigsawId);
            }
            stringRedisTemplate.opsForValue().set(userKey, currentStateJson);
        }

        List<Map<String, Object>> pieces = JSONUtil.toBean(currentStateJson, List.class);
        long total = pieces.size();
        long placedCount = pieces.stream().filter(p -> Boolean.TRUE.equals(p.get("placed"))).count();
        int progress = (int)(placedCount * 100 / total);

        Map<String, Object> response = new HashMap<>();
        response.put("pieces", pieces);
        response.put("progress", progress);

        return Result.success(REQUEST_SUCCESS, response);
    }


}
